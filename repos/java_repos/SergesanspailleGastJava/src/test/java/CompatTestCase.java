import org.junit.jupiter.api.Test;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CompatTestCase {

    private PythonInterpreter interpreter = new PythonInterpreter();

    @Test
    public void testArgAnnotation() {
        interpreter.exec("code = 'def foo(x:int): pass'");
        interpreter.exec("tree = gast.parse(code)");
        interpreter.exec("compile(gast.gast_to_ast(tree), '<test>', 'exec')");
        String norm = "Module(body=[FunctionDef(name='foo', args=arguments(args=[Name(id='x', ctx=Param(), annotation=Name(id='int', ctx=Load(), annotation=None, type_comment=None), type_comment=None)], posonlyargs=[], vararg=None, kwonlyargs=[], kw_defaults=[], kwarg=None, defaults=[]), body=[Pass()], decorator_list=[], returns=None, type_comment=None, type_params=[])], type_ignores=[])";
        interpreter.exec("def dump(node):\n" +
                "    return gast.dump(node, show_empty=True)");
        PyObject evalDump = interpreter.eval("dump(tree)");
        assertEquals(evalDump.toString(), norm);
    }

    @Test
    public void testKeywordOnlyArgument() {
        interpreter.exec("code = 'def foo(*, x=1): pass'");
        interpreter.exec("tree = gast.parse(code)");
        interpreter.exec("compile(gast.gast_to_ast(tree), '<test>', 'exec')");
        String norm = "Module(body=[FunctionDef(name='foo', args=arguments(args=[], posonlyargs=[], vararg=None, kwonlyargs=[Name(id='x', ctx=Param(), annotation=None, type_comment=None)], kw_defaults=[Constant(value=1, kind=None)], kwarg=None, defaults=[]), body=[Pass()], decorator_list=[], returns=None, type_comment=None, type_params=[])], type_ignores=[])";
        interpreter.exec("def dump(node):\n" +
                "    return gast.dump(node, show_empty=True)");
        PyObject evalDump = interpreter.eval("dump(tree)");
        assertEquals(evalDump.toString(), norm);
    }

    @Test
    public void testTryExcept() {
        interpreter.exec("code = 'try:pass\\nexcept e:pass\\nelse:pass'");
        interpreter.exec("tree = gast.parse(code)");
        interpreter.exec("compile(gast.gast_to_ast(tree), '<test>', 'exec')");
        String norm = "Module(body=[Try(body=[Pass()], handlers=[ExceptHandler(type=Name(id='e', ctx=Load(), annotation=None, type_comment=None), name=None, body=[Pass()])], orelse=[Pass()], finalbody=[])], type_ignores=[])";
        interpreter.exec("def dump(node):\n" +
                "    return gast.dump(node, show_empty=True)");
        PyObject evalDump = interpreter.eval("dump(tree)");
        assertEquals(evalDump.toString(), norm);
    }

    @Test
    public void testTryExceptNamed() {
        interpreter.exec("code = 'try:pass\\nexcept e as f:pass\\nelse:pass'");
        interpreter.exec("tree = gast.parse(code)");
        interpreter.exec("compile(gast.gast_to_ast(tree), '<test>', 'exec')");
        String norm = "Module(body=[Try(body=[Pass()], handlers=[ExceptHandler(type=Name(id='e', ctx=Load(), annotation=None, type_comment=None), name=Name(id='f', ctx=Store(), annotation=None, type_comment=None), body=[Pass()])], orelse=[Pass()], finalbody=[])], type_ignores=[])";
        interpreter.exec("def dump(node):\n" +
                "    return gast.dump(node, show_empty=True)");
        PyObject evalDump = interpreter.eval("dump(tree)");
        assertEquals(evalDump.toString(), norm);
    }

    @Test
    public void testRaise() {
        String[] codes = {
            "raise Exception",
            "raise 'Exception'",
            "raise Exception('err')"
        };
        String[] norms = {
            "Module(body=[Raise(exc=Name(id='Exception', ctx=Load(), annotation=None, type_comment=None), cause=None)], type_ignores=[])",
            "Module(body=[Raise(exc=Constant(value='Exception', kind=None), cause=None)], type_ignores=[])",
            "Module(body=[Raise(exc=Call(func=Name(id='Exception', ctx=Load(), annotation=None, type_comment=None), args=[Constant(value='err', kind=None)], keywords=[]), cause=None)], type_ignores=[])"
        };

        for (int i = 0; i < codes.length; i++) {
            interpreter.exec("code = '" + codes[i] + "'");
            interpreter.exec("tree = gast.parse(code)");
            interpreter.exec("compile(gast.gast_to_ast(tree), '<test>', 'exec')");
            interpreter.exec("def dump(node):\n" +
                    "    return gast.dump(node, show_empty=True)");
            PyObject evalDump = interpreter.eval("dump(tree)");
            assertEquals(evalDump.toString(), norms[i]);
        }
    }

    @Test
    public void testCall() {
        interpreter.exec("code = 'foo(x, y=1, *args, **kwargs)'");
        interpreter.exec("tree = gast.parse(code)");
        interpreter.exec("compile(gast.gast_to_ast(tree), '<test>', 'exec')");
        String norm = "Module(body=[Expr(value=Call(func=Name(id='foo', ctx=Load(), annotation=None, type_comment=None), args=[Name(id='x', ctx=Load(), annotation=None, type_comment=None), Starred(value=Name(id='args', ctx=Load(), annotation=None, type_comment=None), ctx=Load())], keywords=[keyword(arg='y', value=Constant(value=1, kind=None)), keyword(arg=None, value=Name(id='kwargs', ctx=Load(), annotation=None, type_comment=None))]))], type_ignores=[])";
        interpreter.exec("def dump(node):\n" +
                "    return gast.dump(node, show_empty=True)");
        PyObject evalDump = interpreter.eval("dump(tree)");
        assertEquals(evalDump.toString(), norm);
    }

    @Test
    public void testWith() {
        interpreter.exec("code = 'with open(\"any\"): pass'");
        interpreter.exec("tree = gast.parse(code)");
        interpreter.exec("compile(gast.gast_to_ast(tree), '<test>', 'exec')");
        String norm = "Module(body=[With(items=[withitem(context_expr=Call(func=Name(id='open', ctx=Load(), annotation=None, type_comment=None), args=[Constant(value='any', kind=None)], keywords=[]), optional_vars=None)], body=[Pass()], type_comment=None)], type_ignores=[])";
        interpreter.exec("def dump(node):\n" +
                "    return gast.dump(node, show_empty=True)");
        PyObject evalDump = interpreter.eval("dump(tree)");
        assertEquals(evalDump.toString(), norm);
    }

    @Test
    public void testTryFinally() {
        interpreter.exec("code = 'try:pass\\nfinally:pass'");
        interpreter.exec("tree = gast.parse(code)");
        interpreter.exec("compile(gast.gast_to_ast(tree), '<test>', 'exec')");
        String norm = "Module(body=[Try(body=[Pass()], handlers=[], orelse=[], finalbody=[Pass()])], type_ignores=[])";
        interpreter.exec("def dump(node):\n" +
                "    return gast.dump(node, show_empty=True)");
        PyObject evalDump = interpreter.eval("dump(tree)");
        assertEquals(evalDump.toString(), norm);
    }

    @Test
    public void testStarArgument() {
        interpreter.exec("code = 'def foo(*a): pass'");
        interpreter.exec("tree = gast.parse(code)");
        interpreter.exec("compile(gast.gast_to_ast(tree), '<test>', 'exec')");
        String norm = "Module(body=[FunctionDef(name='foo', args=arguments(args=[], posonlyargs=[], vararg=Name(id='a', ctx=Param(), annotation=None, type_comment=None), kwonlyargs=[], kw_defaults=[], kwarg=None, defaults=[]), body=[Pass()], decorator_list=[], returns=None, type_comment=None, type_params=[])], type_ignores=[])";
        interpreter.exec("def dump(node):\n" +
                "    return gast.dump(node, show_empty=True)");
        PyObject evalDump = interpreter.eval("dump(tree)");
        assertEquals(evalDump.toString(), norm);
    }

    @Test
    public void testKeywordArgument() {
        interpreter.exec("code = 'def foo(**a): pass'");
        interpreter.exec("tree = gast.parse(code)");
        interpreter.exec("compile(gast.gast_to_ast(tree), '<test>', 'exec')");
        String norm = "Module(body=[FunctionDef(name='foo', args=arguments(args=[], posonlyargs=[], vararg=None, kwonlyargs=[], kw_defaults=[], kwarg=Name(id='a', ctx=Param(), annotation=None, type_comment=None), defaults=[]), body=[Pass()], decorator_list=[], returns=None, type_comment=None, type_params=[])], type_ignores=[])";
        interpreter.exec("def dump(node):\n" +
                "    return gast.dump(node, show_empty=True)");
        PyObject evalDump = interpreter.eval("dump(tree)");
        assertEquals(evalDump.toString(), norm);
    }

    @Test
    public void testIndex() {
        interpreter.exec("code = 'def foo(a): a[1]'");
        interpreter.exec("tree = gast.parse(code)");
        interpreter.exec("compile(gast.gast_to_ast(tree), '<test>', 'exec')");
        String norm = "Module(body=[FunctionDef(name='foo', args=arguments(args=[Name(id='a', ctx=Param(), annotation=None, type_comment=None)], posonlyargs=[], vararg=None, kwonlyargs=[], kw_defaults=[], kwarg=None, defaults=[]), body=[Expr(value=Subscript(value=Name(id='a', ctx=Load(), annotation=None, type_comment=None), slice=Constant(value=1, kind=None), ctx=Load()))], decorator_list=[], returns=None, type_comment=None, type_params=[])], type_ignores=[])";
        interpreter.exec("def dump(node):\n" +
                "    return gast.dump(node, show_empty=True)");
        PyObject evalDump = interpreter.eval("dump(tree)");
        assertEquals(evalDump.toString(), norm);
    }

    @Test
    public void testExtSlice() {
        interpreter.exec("code = 'def foo(a): a[:,:]'");
        interpreter.exec("tree = gast.parse(code)");
        interpreter.exec("compile(gast.gast_to_ast(tree), '<test>', 'exec')");
        String norm = "Module(body=[FunctionDef(name='foo', args=arguments(args=[Name(id='a', ctx=Param(), annotation=None, type_comment=None)], posonlyargs=[], vararg=None, kwonlyargs=[], kw_defaults=[], kwarg=None, defaults=[]), body=[Expr(value=Subscript(value=Name(id='a', ctx=Load(), annotation=None, type_comment=None), slice=Tuple(elts=[Slice(lower=None, upper=None, step=None), Slice(lower=None, upper=None, step=None)], ctx=Load()), ctx=Load()))], decorator_list=[], returns=None, type_comment=None, type_params=[])], type_ignores=[])";
        interpreter.exec("def dump(node):\n" +
                "    return gast.dump(node, show_empty=True)");
        PyObject evalDump = interpreter.eval("dump(tree)");
        assertEquals(evalDump.toString(), norm);
    }

    @Test
    public void testExtSlices() {
        interpreter.exec("code = 'def foo(a): a[1,:]'");
        interpreter.exec("tree = gast.parse(code)");
        interpreter.exec("compile(gast.gast_to_ast(tree), '<test>', 'exec')");
        String norm = "Module(body=[FunctionDef(name='foo', args=arguments(args=[Name(id='a', ctx=Param(), annotation=None, type_comment=None)], posonlyargs=[], vararg=None, kwonlyargs=[], kw_defaults=[], kwarg=None, defaults=[]), body=[Expr(value=Subscript(value=Name(id='a', ctx=Load(), annotation=None, type_comment=None), slice=Tuple(elts=[Constant(value=1, kind=None), Slice(lower=None, upper=None, step=None)], ctx=Load()), ctx=Load()))], decorator_list=[], returns=None, type_comment=None, type_params=[])], type_ignores=[])";
        interpreter.exec("def dump(node):\n" +
                "    return gast.dump(node, show_empty=True)");
        PyObject evalDump = interpreter.eval("dump(tree)");
        assertEquals(evalDump.toString(), norm);
    }

    @Test
    public void testEllipsis() {
        interpreter.exec("code = 'def foo(a): a[...]'");
        interpreter.exec("tree = gast.parse(code)");
        interpreter.exec("compile(gast.gast_to_ast(tree), '<test>', 'exec')");
        String norm = "Module(body=[FunctionDef(name='foo', args=arguments(args=[Name(id='a', ctx=Param(), annotation=None, type_comment=None)], posonlyargs=[], vararg=None, kwonlyargs=[], kw_defaults=[], kwarg=None, defaults=[]), body=[Expr(value=Subscript(value=Name(id='a', ctx=Load(), annotation=None, type_comment=None), slice=Constant(value=Ellipsis, kind=None), ctx=Load()))], decorator_list=[], returns=None, type_comment=None, type_params=[])], type_ignores=[])";
        interpreter.exec("def dump(node):\n" +
                "    return gast.dump(node, show_empty=True)");
        PyObject evalDump = interpreter.eval("dump(tree)");
        assertEquals(evalDump.toString(), norm);
    }

    @Test
    public void testExtSliceEllipsis() {
        interpreter.exec("code = 'def foo(a): a[1, ...]'");
        interpreter.exec("tree = gast.parse(code)");
        interpreter.exec("compile(gast.gast_to_ast(tree), '<test>', 'exec')");
        String norm = "Module(body=[FunctionDef(name='foo', args=arguments(args=[Name(id='a', ctx=Param(), annotation=None, type_comment=None)], posonlyargs=[], vararg=None, kwonlyargs=[], kw_defaults=[], kwarg=None, defaults=[]), body=[Expr(value=Subscript(value=Name(id='a', ctx=Load(), annotation=None, type_comment=None), slice=Tuple(elts=[Constant(value=1, kind=None), Constant(value=Ellipsis, kind=None)], ctx=Load()), ctx=Load()))], decorator_list=[], returns=None, type_comment=None, type_params=[])], type_ignores=[])";
        interpreter.exec("def dump(node):\n" +
                "    return gast.dump(node, show_empty=True)");
        PyObject evalDump = interpreter.eval("dump(tree)");
        assertEquals(evalDump.toString(), norm);
    }
}
