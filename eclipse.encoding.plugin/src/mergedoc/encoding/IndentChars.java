package mergedoc.encoding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

/**
 * TODO michael Provide line separetor related utility functions.
 * @author Shinji Kashihara
 */
public class IndentChars {

    static final String TAB = "TAB";
    static final String SPACE = "SPACE";
    static final String MIXED = "MIXED";


    private IndentChars() {
    }

    /**
     * @param is The input stream will be closed by this operation.
     * @param encoding
     * @return indent character string
     */
    public static String ofContent(InputStream is, String encoding) {
        if (is == null) {
            return null;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new BoundedInputStream(is, 1024 * 1024), encoding));
            return ofContent(reader);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    /**
     * @param reader The reader will NOT be closed by this operation.
     * @return Line separator string
     */
    public static String ofContent(BufferedReader reader) {
        try {
            boolean spaces = false;
            boolean tabs = false;
            String mixed = "MIXED";

            String line;
            while((line = reader.readLine()) != null) {
                for (int i = 0, length = line.length(); i < length; i++) {
                    char c = line.charAt(i);
                    if (Character.isWhitespace(c)) {
                        if (c == ' ') {
                            spaces = true;
                        } else if (c == '\t'){
                            tabs = true;
                        }
                        if (spaces && tabs) {
                            return mixed;
                        }
                    } else {
                        break;
                    }
                }
            }
            if (spaces) {
                return "SPACE";
            }
            if (tabs) {
                return "TAB";
            }
            return null;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String resolve(IResource resource) {
        return ofWorkspace();
    }

    public static String ofWorkspace() {
        IPreferencesService prefs = Platform.getPreferencesService();
        IScopeContext[] scopeContext = new IScopeContext[] { InstanceScope.INSTANCE };
        boolean spacesForTabs = prefs.getBoolean(EditorsUI.PLUGIN_ID, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS, false, scopeContext);
        return toLabel(spacesForTabs);
    }

    public static boolean isMixed(String indentChar) {
        return MIXED.equals(indentChar);
    }

    private static String toLabel(boolean spacesForTabs) {
        return spacesForTabs ? SPACE : TAB;
    }

}
