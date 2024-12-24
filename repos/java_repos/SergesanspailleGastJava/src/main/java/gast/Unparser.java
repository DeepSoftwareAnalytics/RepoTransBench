package gast;

import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

public class Unparser {
    private static PythonInterpreter interpreter = new PythonInterpreter();

    static {
        interpreter.exec("import gast");
    }

    public static String unparse(PyObject astObj) {
        interpreter.set("ast_obj", astObj);
        return interpreter.eval("gast.unparse(ast_obj)").toString();
    }
}
