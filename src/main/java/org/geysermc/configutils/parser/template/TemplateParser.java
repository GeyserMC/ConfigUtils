package org.geysermc.configutils.parser.template;

import it.unimi.dsi.fastutil.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.configutils.file.template.TemplateReader;
import org.geysermc.configutils.parser.placeholder.Placeholders;
import org.geysermc.configutils.parser.template.action.Action;
import org.geysermc.configutils.parser.template.action.ActionResult;
import org.geysermc.configutils.parser.template.action.SingleAction;
import org.geysermc.configutils.parser.template.action.register.RegisteredActions;
import org.geysermc.configutils.parser.template.action.storage.Storable;
import org.geysermc.configutils.parser.template.action.storage.Storables;
import org.geysermc.configutils.parser.template.action.storage.Unfinished;
import org.geysermc.configutils.parser.template.action.storage.predefined.UsedConfigsStorage;

public class TemplateParser implements Storable {
  private final TemplateReader reader;
  private final RegisteredActions actions;

  public TemplateParser(@NonNull TemplateReader reader, @NonNull RegisteredActions actions) {
    this.reader = Objects.requireNonNull(reader);
    this.actions = Objects.requireNonNull(actions);
  }

  @NonNull
  public TemplateParseResult parseTemplate(
      @NonNull String templateName,
      @NonNull Placeholders placeholders) {

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
    storables.add(new UsedConfigsStorage(templateName));

    // allows us to read nested templates
    storables.add(this);

    for (String line : lines) {
      line = placeholders.replacePlaceholders(line);

      Pair<String, Action> action = actions.actionFromLine(line);

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

    List<Unfinished> unfinisheds = storables.allUnfinished();
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
