package org.geysermc.configutils.parser;

import it.unimi.dsi.fastutil.Pair;
import java.util.ArrayList;
import java.util.List;
import org.geysermc.configutils.action.Action;
import org.geysermc.configutils.action.ActionResult;
import org.geysermc.configutils.action.SingleAction;
import org.geysermc.configutils.action.register.RegisteredActions;
import org.geysermc.configutils.action.storage.Storables;
import org.geysermc.configutils.action.storage.Unfinished;
import org.geysermc.configutils.action.storage.predefined.ReadConfigsStorage;
import org.geysermc.configutils.file.template.TemplateReader;
import org.geysermc.configutils.parser.placeholder.Placeholders;

public class TemplateParser {
  private final TemplateReader reader;
  private final RegisteredActions actions;

  public TemplateParser(TemplateReader reader, RegisteredActions actions) {
    this.reader = reader;
    this.actions = actions;
  }

  public TemplateParseResult parseTemplate(String templateName, Placeholders placeholders) {
    List<String> lines;
    try {
      lines = reader.readLines(templateName);
    } catch (Exception e) {
      return TemplateParseResult.failed(new IllegalStateException(String.format(
          "Unable to read template called '%s', does it exist?",
          templateName
      )));
    }

    List<String> templateLines = new ArrayList<>();

    Storables storables = new Storables();
    storables.add(new ReadConfigsStorage(templateName));

    for (String line : lines) {
      Pair<String, Action> action = actions.getActionFromLine(line);

      // if the line isn't an action, it'll be stuff like comments and key/value lines
      if (action == null) {
        templateLines.add(line);
        continue;
      }

      String strippedLine = action.left();

      if (!(action.right() instanceof SingleAction)) {
        return TemplateParseResult.failed(
            new IllegalStateException("Action should be an instance of SingleAction")
        );
      }

      ActionResult result =
          ((SingleAction) action.right()).handle(strippedLine, storables, placeholders, reader);

      if (!result.succeeded()) {
        return TemplateParseResult.failed(
            new IllegalStateException("Got an error while handling action", result.error())
        );
      }

      List<String> linesToAdd = result.linesToAdd();
      if (linesToAdd != null) {
        templateLines.addAll(linesToAdd);
      }
    }

    List<Unfinished> unfinisheds = storables.getAllUnfinished();
    if (!unfinisheds.isEmpty()) {
      StringBuilder builder = new StringBuilder();
      for (Unfinished unfinished : unfinisheds) {
        if (builder.length() > 0) {
          builder.append(", ");
        }
        builder.append(unfinished.friendlyName());
      }
      builder.insert(0, "Template is not complete! The following hasn't been finished: ");
      return TemplateParseResult.failed(new IllegalStateException(builder.toString()));
    }

    return TemplateParseResult.ok(templateLines, storables);
  }
}
