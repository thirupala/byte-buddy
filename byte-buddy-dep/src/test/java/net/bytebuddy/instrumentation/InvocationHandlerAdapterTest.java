package net.bytebuddy.instrumentation;

import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.CallTraceable;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;

public class InvocationHandlerAdapterTest extends AbstractInstrumentationTest {

    public static final String FOO = "foo", BAR = "bar", QUX = "qux";

    @Test
    public void testStaticAdapterWithoutCache() throws Exception {
        Foo foo = new Foo();
        DynamicType.Loaded<Bar> loaded = instrument(Bar.class, InvocationHandlerAdapter.of(foo));
        assertThat(loaded.getLoadedAuxiliaryTypes().size(), is(0));
        assertThat(loaded.getLoaded().getDeclaredMethods().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredFields().length, is(1));
        Bar instance = loaded.getLoaded().newInstance();
        assertThat(instance.bar(FOO), is((Object) instance));
        assertThat(foo.methods.size(), is(1));
        assertThat(instance.bar(FOO), is((Object) instance));
        assertThat(foo.methods.size(), is(2));
        assertThat(foo.methods.get(0), not(sameInstance(foo.methods.get(1))));
        instance.assertZeroCalls();
    }

    @Test
    public void testStaticAdapterWithMethodCache() throws Exception {
        Foo foo = new Foo();
        DynamicType.Loaded<Bar> loaded = instrument(Bar.class, InvocationHandlerAdapter.of(foo).withMethodCache());
        assertThat(loaded.getLoadedAuxiliaryTypes().size(), is(0));
        assertThat(loaded.getLoaded().getDeclaredMethods().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredFields().length, is(2));
        Bar instance = loaded.getLoaded().newInstance();
        assertThat(instance.bar(FOO), is((Object) instance));
        assertThat(foo.methods.size(), is(1));
        assertThat(instance.bar(FOO), is((Object) instance));
        assertThat(foo.methods.size(), is(2));
        assertThat(foo.methods.get(0), sameInstance(foo.methods.get(1)));
        instance.assertZeroCalls();
    }

    @Test
    public void testInstanceAdapterWithoutCache() throws Exception {
        DynamicType.Loaded<Bar> loaded = instrument(Bar.class, InvocationHandlerAdapter.toInstanceField(QUX));
        assertThat(loaded.getLoadedAuxiliaryTypes().size(), is(0));
        assertThat(loaded.getLoaded().getDeclaredMethods().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredFields().length, is(1));
        Field field = loaded.getLoaded().getDeclaredField(QUX);
        assertThat(field.getModifiers(), is(Modifier.PUBLIC));
        field.setAccessible(true);
        Bar instance = loaded.getLoaded().newInstance();
        Foo foo = new Foo();
        field.set(instance, foo);
        assertThat(instance.bar(FOO), is((Object) instance));
        assertThat(foo.methods.size(), is(1));
        assertThat(instance.bar(FOO), is((Object) instance));
        assertThat(foo.methods.size(), is(2));
        assertThat(foo.methods.get(0), not(sameInstance(foo.methods.get(1))));
        instance.assertZeroCalls();
    }

    @Test
    public void testInstanceAdapterWithMethodCache() throws Exception {
        DynamicType.Loaded<Bar> loaded = instrument(Bar.class, InvocationHandlerAdapter.toInstanceField(QUX).withMethodCache());
        assertThat(loaded.getLoadedAuxiliaryTypes().size(), is(0));
        assertThat(loaded.getLoaded().getDeclaredMethods().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredFields().length, is(2));
        Field field = loaded.getLoaded().getDeclaredField(QUX);
        assertThat(field.getModifiers(), is(Modifier.PUBLIC));
        field.setAccessible(true);
        Bar instance = loaded.getLoaded().newInstance();
        Foo foo = new Foo();
        field.set(instance, foo);
        assertThat(instance.bar(FOO), is((Object) instance));
        assertThat(foo.methods.size(), is(1));
        assertThat(instance.bar(FOO), is((Object) instance));
        assertThat(foo.methods.size(), is(2));
        assertThat(foo.methods.get(0), sameInstance(foo.methods.get(1)));
        instance.assertZeroCalls();
    }

    @Test
    public void testEqualsHashCodeStaticAdapter() throws Exception {
        assertThat(InvocationHandlerAdapter.of(new Foo(FOO)).hashCode(), is(InvocationHandlerAdapter.of(new Foo(FOO)).hashCode()));
        assertThat(InvocationHandlerAdapter.of(new Foo(FOO)), is(InvocationHandlerAdapter.of(new Foo(FOO))));
        assertThat(InvocationHandlerAdapter.of(new Foo(FOO)).hashCode(), not(is(InvocationHandlerAdapter.of(new Foo(BAR)).hashCode())));
        assertThat(InvocationHandlerAdapter.of(new Foo(FOO)), not(is(InvocationHandlerAdapter.of(new Foo(BAR)))));
        assertThat(InvocationHandlerAdapter.of(new Foo(FOO)).hashCode(), not(is(InvocationHandlerAdapter.of(new Foo(FOO), QUX).hashCode())));
        assertThat(InvocationHandlerAdapter.of(new Foo(FOO)), not(is(InvocationHandlerAdapter.of(new Foo(FOO), QUX))));
        assertThat(InvocationHandlerAdapter.of(new Foo(FOO), QUX).hashCode(), not(is(InvocationHandlerAdapter.toInstanceField(QUX).hashCode())));
        assertThat(InvocationHandlerAdapter.of(new Foo(FOO), QUX), not(is(InvocationHandlerAdapter.toInstanceField(QUX))));
    }

    @Test
    public void testEqualsHashCodeInstanceAdapter() throws Exception {
        assertThat(InvocationHandlerAdapter.toInstanceField(QUX).hashCode(), is(InvocationHandlerAdapter.toInstanceField(QUX).hashCode()));
        assertThat(InvocationHandlerAdapter.toInstanceField(QUX), is(InvocationHandlerAdapter.toInstanceField(QUX)));
        assertThat(InvocationHandlerAdapter.toInstanceField(QUX).hashCode(), not(is(InvocationHandlerAdapter.toInstanceField(FOO).hashCode())));
        assertThat(InvocationHandlerAdapter.toInstanceField(QUX), not(is(InvocationHandlerAdapter.toInstanceField(FOO))));
        assertThat(InvocationHandlerAdapter.toInstanceField(QUX).hashCode(), not(is(InvocationHandlerAdapter.of(new Foo(BAR), QUX).hashCode())));
        assertThat(InvocationHandlerAdapter.toInstanceField(QUX), not(is(InvocationHandlerAdapter.of(new Foo(BAR), QUX))));
    }

    private static class Foo implements InvocationHandler {

        private final String marker;

        public List<Method> methods;

        private Foo() {
            marker = FOO;
            methods = new LinkedList<Method>();
        }

        private Foo(String marker) {
            this.marker = marker;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            methods.add(method);
            assertThat(args.length, is(1));
            assertThat(args[0], is((Object) FOO));
            assertThat(method.getName(), is(BAR));
            assertThat(proxy, instanceOf(Bar.class));
            return proxy;
        }

        @Override
        public boolean equals(Object other) {
            return this == other || !(other == null || getClass() != other.getClass())
                    && marker.equals(((Foo) other).marker);
        }

        @Override
        public int hashCode() {
            return marker.hashCode();
        }
    }

    public static class Bar extends CallTraceable {

        public Object bar(Object o) {
            register(BAR);
            return o;
        }
    }
}