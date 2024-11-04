import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Microdata {

    public static List<Item> getItems(String filePath) throws IOException {
        Document document = Jsoup.parse(Microdata.class.getResourceAsStream(filePath), "UTF-8", "");
        return findItems(document);
    }

    private static List<Item> findItems(Element element) {
        List<Item> items = new ArrayList<>();
        Elements elements = element.getElementsByAttribute("itemscope");
        for (Element el : elements) {
            Item item = makeItem(el);
            extractProperties(el, item);
            items.add(item);
        }
        return items;
    }

    private static void extractProperties(Element element, Item item) {
        Elements children = element.children();
        for (Element child : children) {
            String itemprop = child.attr("itemprop");
            if (!itemprop.isEmpty()) {
                if (child.hasAttr("itemscope")) {
                    Item nestedItem = makeItem(child);
                    extractProperties(child, nestedItem);
                    item.set(itemprop, nestedItem);
                } else {
                    item.set(itemprop, getPropertyValue(child));
                    extractProperties(child, item);
                }
            } else if (child.hasAttr("itemscope")) {
                extractProperties(child, item);
            }
        }
    }

    private static String getPropertyValue(Element element) {
        String tagName = element.tagName();
        if (tagName.equals("a") || tagName.equals("img") || tagName.equals("area") || tagName.equals("link") ||
            tagName.equals("audio") || tagName.equals("embed") || tagName.equals("iframe") || tagName.equals("source") || 
            tagName.equals("video") || tagName.equals("object") || tagName.equals("time")) {
            return element.attr("src");
        }
        return element.text();
    }

    private static Item makeItem(Element element) {
        String itemtype = element.attr("itemtype");
        String itemid = element.attr("itemid");
        return new Item(itemtype, itemid);
    }
}
