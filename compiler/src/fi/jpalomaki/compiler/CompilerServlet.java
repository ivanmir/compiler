package fi.jpalomaki.compiler;

import static com.google.common.io.Files.createTempDir;
import static java.io.File.createTempFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Simple Java compiler servlet.
 * 
 * @author jpalomaki
 */
public final class CompilerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        File workingDirectory = createTempDir();
        try {
            File javaFile = createTempFile("tmp", ".java", workingDirectory);
            extractSourceCodeAndSaveToJavaFile(request, javaFile);
            String jarpath = request.getParameter("classpath1");
            
            jarpath = this.getServletContext().getRealPath(jarpath);
            
            
            String classpath = System.getProperty("java.class.path") + File.pathSeparator + jarpath;
            
            if (Files.isReadable(Paths.get(jarpath))) {
                File classFile = compile(javaFile, classpath);
                writeResponse(classFile, response);
            } else {
            	response.getWriter().println("Unable to find jar file at specified path: " + jarpath);
            }
            
            


        } finally {
            FileUtils.deleteQuietly(workingDirectory);
        }
    }

    private File extractSourceCodeAndSaveToJavaFile(HttpServletRequest request, File javaFile) throws IOException {
        String source = request.getParameter("source");
        InputStream input = new ByteArrayInputStream(source.getBytes());
        OutputStream output = new FileOutputStream(javaFile);
        try {
            IOUtils.copy(input, output);
        } finally {
            IOUtils.closeQuietly(output);
        }
        return javaFile;
    }
    
    private File compile(File javaFile, String classpath) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compilationUnit = fileManager.getJavaFileObjects(javaFile);
        List<String> options = classpath != null ? Arrays.asList("-classpath", classpath) : null;
        StringWriter output = new StringWriter();
        try {
            boolean successful = compiler.getTask(output, fileManager, null, options, null, compilationUnit).call();
            if (!successful) {
                throw new CompilationException("Failed to compile: " + javaFile, output.toString());
            }
            return firstClassFileFrom(javaFile.getParentFile());
        } finally {
            fileManager.close();
        }
    }
    
    private File firstClassFileFrom(File parent) {
        return parent.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".class");
            }
        })[0];
    }

    private void writeResponse(File classFile, HttpServletResponse response) throws IOException {
        InputStream inputStream = new FileInputStream(classFile);
        try {
            response.setContentLength((int)classFile.length());
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + classFile.getName() + "\"");
            ServletOutputStream outputStream = response.getOutputStream();
            IOUtils.copy(inputStream, outputStream);
            outputStream.flush();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }
}

final class CompilationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CompilationException(String message, String output) {
        super(message + "; caused by:\n\n" + output);
    }
}
