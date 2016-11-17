package nl.gertjanal.metaltools.jstree;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.parsingdata.metal.data.ParseGraph;
import io.parsingdata.metal.data.ParseItem;
import io.parsingdata.metal.data.ParseValue;
import io.parsingdata.metal.token.Def;
import io.parsingdata.metal.token.Token;

public class JsTree {

    public static void generate() {

    }

    public static String generateJs(final ParseGraph graph) throws IOException {
        final Map<String, Object> root = new LinkedHashMap<>();
        step(graph, root, new HashMap<>());
        final ObjectMapper objectMapper = new ObjectMapper();
        //return objectMapper.writeValueAsString(((List<?>) root.get("children")).get(0));
        return objectMapper.writeValueAsString(root);
    }

    private static void step(final ParseItem item, final Map<String, Object> self, final Map<Token, LinkedList<Map<String, Object>>> collection) {
        final Token definition = item.getDefinition();
        self.put("name", definition.getClass().getSimpleName() + (definition.name.isEmpty() ? "" : ": " + definition.name));
        if (!item.isGraph()) {
            if (item.getDefinition() instanceof Def) {
                final ParseValue value = item.asValue();
                final byte[] bytes = value.getValue();
                if (bytes.length < 255) {
                    self.put("value", bytes);
                }
            }
            return;
        }
        if (item.asGraph().head == null) {
            return;
        }

        final LinkedList<Map<String, Object>> children;
        final Map<String, Object> head = new LinkedHashMap<>();
        final Map<String, Object> tail = new LinkedHashMap<>();

        if (collection.containsKey(definition)) {
            children = collection.get(definition);
        }
        else {
            children = new LinkedList<>();
            self.put("children", children);
            collection.put(definition, children);
        }

        children.push(head);

        if (item.asGraph().tail.getDefinition() != item.getDefinition()) {
            children.push(tail);
        }

        step(item.asGraph().tail, tail, collection);
        step(item.asGraph().head, head, collection);
    }
}
