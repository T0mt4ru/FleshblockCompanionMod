package net.minecraft.client.searchtree;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import java.util.Comparator;
import java.util.Iterator;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IntersectionIterator<T> extends AbstractIterator<T> {
    private final PeekingIterator<T> firstIterator;
    private final PeekingIterator<T> secondIterator;
    private final Comparator<T> comparator;

    public IntersectionIterator(Iterator<T> firstIterator, Iterator<T> secondIterator, Comparator<T> comparator) {
        this.firstIterator = Iterators.peekingIterator(firstIterator);
        this.secondIterator = Iterators.peekingIterator(secondIterator);
        this.comparator = comparator;
    }

    @Override
    protected T computeNext() {
        while (this.firstIterator.hasNext() && this.secondIterator.hasNext()) {
            int i = this.comparator.compare(this.firstIterator.peek(), this.secondIterator.peek());
            if (i == 0) {
                this.secondIterator.next();
                return this.firstIterator.next();
            }

            if (i < 0) {
                this.firstIterator.next();
            } else {
                this.secondIterator.next();
            }
        }

        return this.endOfData();
    }
}
