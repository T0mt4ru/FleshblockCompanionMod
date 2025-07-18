package net.minecraft.world.level.validation;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class DirectoryValidator {
    private final PathMatcher symlinkTargetAllowList;

    public DirectoryValidator(PathMatcher symlinkTargetAllowList) {
        this.symlinkTargetAllowList = symlinkTargetAllowList;
    }

    public void validateSymlink(Path directory, List<ForbiddenSymlinkInfo> entries) throws IOException {
        Path path = Files.readSymbolicLink(directory);
        if (!this.symlinkTargetAllowList.matches(path)) {
            entries.add(new ForbiddenSymlinkInfo(directory, path));
        }
    }

    public List<ForbiddenSymlinkInfo> validateSymlink(Path directory) throws IOException {
        List<ForbiddenSymlinkInfo> list = new ArrayList<>();
        this.validateSymlink(directory, list);
        return list;
    }

    public List<ForbiddenSymlinkInfo> validateDirectory(Path directory, boolean validateSymlinks) throws IOException {
        List<ForbiddenSymlinkInfo> list = new ArrayList<>();

        BasicFileAttributes basicfileattributes;
        try {
            basicfileattributes = Files.readAttributes(directory, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        } catch (NoSuchFileException nosuchfileexception) {
            return list;
        }

        if (basicfileattributes.isRegularFile()) {
            throw new IOException("Path " + directory + " is not a directory");
        } else {
            if (basicfileattributes.isSymbolicLink()) {
                if (!validateSymlinks) {
                    this.validateSymlink(directory, list);
                    return list;
                }

                directory = Files.readSymbolicLink(directory);
            }

            this.validateKnownDirectory(directory, list);
            return list;
        }
    }

    public void validateKnownDirectory(Path directory, final List<ForbiddenSymlinkInfo> forbiddenSymlinkInfos) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            private void validateSymlink(Path path, BasicFileAttributes attributes) throws IOException {
                if (attributes.isSymbolicLink()) {
                    DirectoryValidator.this.validateSymlink(path, forbiddenSymlinkInfos);
                }
            }

            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) throws IOException {
                this.validateSymlink(dir, attributes);
                return super.preVisitDirectory(dir, attributes);
            }

            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                this.validateSymlink(file, attributes);
                return super.visitFile(file, attributes);
            }
        });
    }
}
