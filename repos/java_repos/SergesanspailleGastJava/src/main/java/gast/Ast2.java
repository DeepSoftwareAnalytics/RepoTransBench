package gast;

import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

public class Ast2 {
    private static PythonInterpreter interpreter = new PythonInterpreter();

    static {
        interpreter.exec("import ast");
        interpreter.exec("import gast");
    }

    public static PyObject astToGast(PyObject node) {
        interpreter.set("node", node);
        return interpreter.eval("gast.ast_to_gast(node)");
    }

    public static PyObject gastToAst(PyObject node) {
        interpreter.set("node", node);
        return interpreter.eval("gast.gast_to_ast(node)");
    }
}
