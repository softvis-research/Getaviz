package org.svis.xtext.formatting;

import org.eclipse.xtext.formatting.impl.AbstractDeclarativeFormatter;
import org.eclipse.xtext.formatting.impl.FormattingConfig;
import org.svis.xtext.services.DynamixGrammarAccess;

/**
 * This class contains custom formatting description.
 *
 * see : http://www.eclipse.org/Xtext/documentation/latest/xtext.html#formatting
 * on how and when to use it
 *
 * Also see {@link org.eclipse.xtext.xtext.XtextFormattingTokenSerializer} as an
 * example
 */
public class DynamixFormatter extends AbstractDeclarativeFormatter {

  @Override
  protected void configureFormatting(FormattingConfig c) {
    DynamixGrammarAccess f = (DynamixGrammarAccess) getGrammarAccess();
    c.setAutoLinewrap(120);
    c.setLinewrap(1, 2, 3).around(f.getDynamixElementRule());
    
//    c.setLinewrap(1, 1, 2).around(f.getFeatureRule());
//    List<Pair<Keyword,Keyword>> pairs = f.findKeywordPairs("{", "}");
//    for (Pair<Keyword, Keyword> pair : pairs) {
//    c.setIndentation(pair.getFirst(), pair.getSecond());
//    }
    c.setLinewrap(0, 1, 2).before(f.getSL_COMMENTRule());
    c.setLinewrap(0, 1, 2).before(f.getML_COMMENTRule());
    c.setLinewrap(0, 1, 1).after(f.getML_COMMENTRule());
  }
}
