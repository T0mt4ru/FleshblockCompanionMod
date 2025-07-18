package net.minecraft.server.advancements;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.DisplayInfo;

public class AdvancementVisibilityEvaluator {
    private static final int VISIBILITY_DEPTH = 2;

    private static AdvancementVisibilityEvaluator.VisibilityRule evaluateVisibilityRule(Advancement advancement, boolean alwaysShow) {
        Optional<DisplayInfo> optional = advancement.display();
        if (optional.isEmpty()) {
            return AdvancementVisibilityEvaluator.VisibilityRule.HIDE;
        } else if (alwaysShow) {
            return AdvancementVisibilityEvaluator.VisibilityRule.SHOW;
        } else {
            return optional.get().isHidden() ? AdvancementVisibilityEvaluator.VisibilityRule.HIDE : AdvancementVisibilityEvaluator.VisibilityRule.NO_CHANGE;
        }
    }

    private static boolean evaluateVisiblityForUnfinishedNode(Stack<AdvancementVisibilityEvaluator.VisibilityRule> visibilityRules) {
        for (int i = 0; i <= 2; i++) {
            AdvancementVisibilityEvaluator.VisibilityRule advancementvisibilityevaluator$visibilityrule = visibilityRules.peek(i);
            if (advancementvisibilityevaluator$visibilityrule == AdvancementVisibilityEvaluator.VisibilityRule.SHOW) {
                return true;
            }

            if (advancementvisibilityevaluator$visibilityrule == AdvancementVisibilityEvaluator.VisibilityRule.HIDE) {
                return false;
            }
        }

        return false;
    }

    private static boolean evaluateVisibility(
        AdvancementNode advancement,
        Stack<AdvancementVisibilityEvaluator.VisibilityRule> visibilityRules,
        Predicate<AdvancementNode> predicate,
        AdvancementVisibilityEvaluator.Output output
    ) {
        boolean flag = predicate.test(advancement);
        AdvancementVisibilityEvaluator.VisibilityRule advancementvisibilityevaluator$visibilityrule = evaluateVisibilityRule(advancement.advancement(), flag);
        boolean flag1 = flag;
        visibilityRules.push(advancementvisibilityevaluator$visibilityrule);

        for (AdvancementNode advancementnode : advancement.children()) {
            flag1 |= evaluateVisibility(advancementnode, visibilityRules, predicate, output);
        }

        boolean flag2 = flag1 || evaluateVisiblityForUnfinishedNode(visibilityRules);
        visibilityRules.pop();
        output.accept(advancement, flag2);
        return flag1;
    }

    public static void evaluateVisibility(AdvancementNode advancement, Predicate<AdvancementNode> predicate, AdvancementVisibilityEvaluator.Output output) {
        AdvancementNode advancementnode = advancement.root();
        Stack<AdvancementVisibilityEvaluator.VisibilityRule> stack = new ObjectArrayList<>();

        for (int i = 0; i <= 2; i++) {
            stack.push(AdvancementVisibilityEvaluator.VisibilityRule.NO_CHANGE);
        }

        evaluateVisibility(advancementnode, stack, predicate, output);
    }

    public static boolean isVisible(AdvancementNode advancement, Predicate<AdvancementNode> test) {
        Stack<AdvancementVisibilityEvaluator.VisibilityRule> stack = new ObjectArrayList<>();

        for(int i = 0; i <= 2; ++i) {
            stack.push(AdvancementVisibilityEvaluator.VisibilityRule.NO_CHANGE);
        }
        return evaluateVisibility(advancement.root(), stack, test, (adv, visible) -> {});
    }

    @FunctionalInterface
    public interface Output {
        void accept(AdvancementNode advancement, boolean visible);
    }

    static enum VisibilityRule {
        SHOW,
        HIDE,
        NO_CHANGE;
    }
}
