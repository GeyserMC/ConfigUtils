package org.geysermc.configutils.parser.template.action.predefined;

import java.util.ArrayList;
import java.util.List;
import org.geysermc.configutils.file.template.TemplateReader;
import org.geysermc.configutils.parser.placeholder.Placeholders;
import org.geysermc.configutils.parser.template.action.ActionResult;
import org.geysermc.configutils.parser.template.action.SingleAction;
import org.geysermc.configutils.parser.template.action.storage.Singleton;
import org.geysermc.configutils.parser.template.action.storage.Storable;
import org.geysermc.configutils.parser.template.action.storage.Storables;
import org.geysermc.configutils.parser.template.action.storage.Unfinished;

/**
 * The action responsible for importing a section to another config file. `>>| 10` is used to add
 * the provided section at line 10 of the other config, and `>>|` is used to define the end of the
 * section to add.
 */
public class ImportSectionAction implements SingleAction, Storable, Singleton, Unfinished {
  private int line;

  @Override
  public String friendlyName() {
    return "import section";
  }

  @Override
  public char actionPrefix() {
    return '|';
  }

  @Override
  public ActionResult handle(
      String strippedLine,
      Storables storables,
      Placeholders placeholders,
      TemplateReader reader) {

    // close import section
    if (strippedLine.length() <= 1) {
      ImportSectionAction importSection = storables.removeFirst(ImportSectionAction.class);
      if (importSection == null) {
        return ActionResult.failed("Cannot close an section that wasn't opened");
      }
      storables.add(new LastImportSection(importSection.line()));
      return ActionResult.ok();
    }

    // start import section

    Unfinished unfinished = storables.firstUnfinished(ImportSectionAction.class);
    if (unfinished != null) {
      return ActionResult.failed(unfinished.unfinishedMessage(this));
    }

    DefineImportAction defineImport = storables.first(DefineImportAction.class);
    if (defineImport == null) {
      return ActionResult.failed("Cannot start insert without providing a config to import from");
    }

    int line;
    try {
      line = Integer.parseInt(strippedLine.substring(1)) - 1;
    } catch (NumberFormatException e) {
      return ActionResult.failed(String.format("'%s' is not a valid number", strippedLine));
    }

    if (line < 0) {
      return ActionResult.failed("Cannot read a negative line number");
    }
    this.line = line;

    storables.add(this); // store the line for later use in the close import section

    int startLine = 0;
    LastImportSection lastImport = storables.removeFirst(LastImportSection.class);
    if (lastImport != null) {
      startLine = lastImport.lastLine();
    }

    List<String> lines = defineImport.lines();

    List<String> linesToAdd = new ArrayList<>();
    for (int i = startLine; i < line; i++) {
      linesToAdd.add(placeholders.runPlaceholder(lines.get(i)));
    }

    return ActionResult.addLines(linesToAdd);
  }

  public int line() {
    return line;
  }

  @Override
  public SingleAction newInstance() {
    return new ImportSectionAction();
  }

  static class LastImportSection implements Storable, Singleton {
    private final int lastLine;

    public LastImportSection(int lastLine) {
      this.lastLine = lastLine;
    }

    public int lastLine() {
      return lastLine;
    }
  }
}
