package net.minecraft.client.searchtree;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import java.util.Comparator;
import java.util.Iterator;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MergingUniqueIterator<T> extends AbstractIterator<T> {
    private final PeekingIterator<T> firstIterator;
    private final PeekingIterator<T> secondIterator;
    private final Comparator<T> comparator;

    public MergingUniqueIterator(Iterator<T> firstIterator, Iterator<T> secondIterator, Comparator<T> comparator) {
        this.firstIterator = Iterators.peekingIterator(firstIterator);
        this.secondIterator = Iterators.peekingIterator(secondIterator);
        this.comparator = comparator;
    }

    @Override
    protected T computeNext() {
        boolean flag = !this.firstIterator.hasNext();
        boolean flag1 = !this.secondIterator.hasNext();
        if (flag && flag1) {
            return this.endOfData();
        } else if (flag) {
            return this.secondIterator.next();
        } else if (flag1) {
            return this.firstIterator.next();
        } else {
            int i = this.comparator.compare(this.firstIterator.peek(), this.secondIterator.peek());
            if (i == 0) {
                this.secondIterator.next();
            }

            return i <= 0 ? this.firstIterator.next() : this.secondIterator.next();
        }
    }
}
