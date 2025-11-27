package app.services.svg;

public class Svg {
    private static final String SVG_TEMPLATE = "<svg version=\"1.1\"\n" +
            "     x=\"%d\" y=\"%d\"\n" +
            "     width=\"%s\" \n" +
            "     viewBox=\"%s\" \n" +
            "     preserveAspectRatio=\"xMinYMin\">";
    private static final String SVG_TEMPLATE_INNER = "<svg version=\"1.1\"\n" +
            "     x=\"%d\" y=\"%d\"\n" +
            "     width=\"%d\" \n" +
            "     height=\"%d\" \n" +
            "     viewBox=\"%s\" \n" +
            "     preserveAspectRatio=\"xMinYMin\">";
    private static final String SVG_RECT_TEMPLATE = "<rect x=\"%f\" y=\"%f\" height=\"%f\" width=\"%f\" style=\"%s\" />";
    private static final String SVG_ARROW_DEFS = "<defs>\n" +
            "        <marker\n" +
            "                id=\"beginArrow\"\n" +
            "                markerWidth=\"12\"\n" +
            "                markerHeight=\"12\"\n" +
            "                refX=\"0\"\n" +
            "                refY=\"6\"\n" +
            "                orient=\"auto\">\n" +
            "            <path d=\"M0,6 L12,0 L12,12 L0,6\" style=\"fill: #000000;\" />\n" +
            "        </marker>\n" +
            "        <marker\n" +
            "                id=\"endArrow\"\n" +
            "                markerWidth=\"12\"\n" +
            "                markerHeight=\"12\"\n" +
            "                refX=\"12\"\n" +
            "                refY=\"6\"\n" +
            "                orient=\"auto\">\n" +
            "            <path d=\"M0,0 L12,6 L0,12 L0,0 \" style=\"fill: #000000;\" />\n" +
            "        </marker>\n" +
            "    </defs>";
    private static final String SVG_LINE_TEMPLATE = "<line x1=\"%f\" y1=\"%f\" x2=\"%f\" y2=\"%f\" style=\"%s\"/>";
    private static final String SVG_TEXT_TEMPLATE = "<text style=\"text-anchor: middle\" transform=\"translate(%.2f,%.2f) rotate(%.2f)\">%s</text>";
    private static final String SVG_LINE_WITH_ARROWS_TEMPLATE = "<line x1=\"%f\"  y1=\"%f\" x2=\"%f\"   y2=\"%f\"\n" +
            "          style=\"stroke:#000000;\n" +
            " marker-start: url(#beginArrow);\n" +
            "marker-end: url(#endArrow);\"/>";

    private StringBuilder svg = new StringBuilder();

    public Svg(int x, int y, String width, String viewBox)
    {
        svg.append(String.format(SVG_TEMPLATE, x, y, width, viewBox));
    }

    public Svg(int x, int y, int length, int width, String viewBox)
    {
        svg.append(String.format(SVG_TEMPLATE_INNER, x, y, length, width, viewBox));
    }


    public void addRectangle(double x, double y, double height, double width, String style)
    {
        svg.append(String.format(SVG_RECT_TEMPLATE, x, y, height, width, style));

    }
    public void addLine(double x1, double y1, double x2, double y2, String style)
    {
        svg.append(String.format(SVG_LINE_TEMPLATE, x1, y1, x2, y2, style));
    }

    public void addLineWithArrows(double x1, double y1, double x2, double y2)
    {
        svg.append(String.format(SVG_LINE_WITH_ARROWS_TEMPLATE, x1, y1, x2, y2));

    }
    public void addText(double x, double y, double rotation, String text)
    {
        svg.append(String.format(SVG_TEXT_TEMPLATE, x, y, rotation, text));

    }
    public void addSvg(Svg innerSvg)
    {
        svg.append(innerSvg).toString();
    }

    public void addArrowDefs()
    {
        svg.append(SVG_ARROW_DEFS);
    }

    @Override
    public String toString() {
        return svg.append("</svg>").toString();
    }
}