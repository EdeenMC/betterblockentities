package betterblockentities.client.render.immediate.overlay;

/* minecraft */
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;

/* java/misc */
import java.util.ArrayDeque;
import java.util.Deque;

public class OverlayNodeStorage extends SubmitNodeStorage {
    public final SubmitStack stack = new SubmitStack();

    @Override
    public SubmitNodeCollection order(final int order) {
        return this.submitsPerOrder.computeIfAbsent(order, ignored -> new OverlayNodeCollection(order, this.stack));
    }

    public static final class SubmitStack {
        private final Deque<SubmitParameters> stack = new ArrayDeque<>();

        public Scope push(SubmitParameters parameters) {
            this.stack.addLast(parameters);
            return new Scope(this);
        }

        private void pop() {
            if (this.stack.isEmpty()) {
                throw new IllegalStateException("Cannot pop an empty stack");
            }

            this.stack.removeLast();
        }

        public SubmitParameters last() {
            if (this.stack.isEmpty()) {
                throw new IllegalStateException("Cannot read from an empty stack");
            }

            return this.stack.getLast();
        }
    }

    public static final class Scope implements AutoCloseable {
        private final SubmitStack stack;
        private boolean closed;

        private Scope(SubmitStack stack) {
            this.stack = stack;
        }

        @Override
        public void close() {
            if (!this.closed) {
                this.closed = true;
                this.stack.pop();
            }
        }
    }

    @FunctionalInterface
    public interface SubmitResolver {
        SubmitResolution resolve(SubmitCall call);
    }

    public record SubmitCall(int order, Model<?> model, Object state) { }

    public record SubmitResolution(OverlayDrawPhase phase, Object modelStateOverride) { }

    public record SubmitParameters(SubmitResolver resolver) { }
}
