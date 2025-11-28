package app.services.svg;

import app.dto.RafterCalculationDTO;
import app.entities.Carport;
import app.util.PartCalculator;

public class CarportSvgSide
{
    private Carport carport;
    private Svg carportSideSvg;
    private Svg carportInnerSvg;
    private final String VIEW_BOX = "0 0 1000 500";
    private final String WIDTH_SIZE = "100%";
    private final String BASE_STYLE = "stroke-width: 1px; stroke:#000000; fill: #ffffff";
    private final int CARPORT_TOP_HEIGHT_CM = 230;
    private final int CARPORT_TOP_HEIGHT_END_CM = 220;
    private final double CARPORT__HEIGHT_OFFSET_TO_TOP_BEAM = 20;
    private final double BEAM_HEIGHT_CM =  19.5;
    private final double WEATHER_BOARD_HEIGHT_CM = 10.0;
    private final double POST_WIDTH_CM = 10.0;
    private final double POST_HEIGHT_CM = 210.0 - BEAM_HEIGHT_CM;
    private final double POST_START_POSITION_CM = 100.0;

    private double yPositionBottom;
    private double yPositionTop;
    private final int INNER_SVG_X_START = 150;
    private final int INNER_SVG_Y_START = 50;
    double arrowLeftXStart;
    double arrowInnerLeftXStart;
    double arrowBottomY;
    double arrowBottomXEnd;
    double arrowYBottomMargin;
    double arrowYTopMargin;

    // Verify
    private final double POST_MAX_SPAN_CM = 310.0;
    private final double POST_FRONT_PLACEMENT_CM = 100.0;
    private final double POST_CENTER_PLACEMENT_CM = POST_FRONT_PLACEMENT_CM + POST_MAX_SPAN_CM;
    private double POST_BACK_PLACEMENT_CM;
    private final double MAX_SPACING_CM = 55.0;
    private final double POST_EDGE_INSET_CM = 35.00;


    public CarportSvgSide(Carport carport)
    {
        this.carport = carport;
        this.carportSideSvg = new Svg(0, 0, WIDTH_SIZE, VIEW_BOX);
        this.carportInnerSvg = new Svg(INNER_SVG_X_START, INNER_SVG_Y_START, carport.getLength(), CARPORT_TOP_HEIGHT_CM, getInnerViewBox(carport.getWidth(), carport.getLength()));
        this.yPositionBottom = CARPORT_TOP_HEIGHT_CM;
        this.yPositionTop = 0;

        this.arrowLeftXStart = INNER_SVG_X_START / 2;
        this.arrowInnerLeftXStart = INNER_SVG_X_START * 0.75;
        this.arrowBottomY = CARPORT_TOP_HEIGHT_CM + INNER_SVG_Y_START;
        this.arrowBottomXEnd = CARPORT_TOP_HEIGHT_CM + INNER_SVG_X_START;
        this.arrowYBottomMargin = CARPORT_TOP_HEIGHT_CM + (INNER_SVG_Y_START * 1.5);
        this.arrowYTopMargin = INNER_SVG_Y_START * 0.75;

        carportSideSvg.addArrowDefs();
        addFrame();
        addPost();
        addRafters();
        addBeamAndWeatherBoard();
        addArrows();
        addArrowText();


        carportSideSvg.addSvg(carportInnerSvg);
    }

    private void addFrame()
    {
        double measureLineFront = CARPORT_TOP_HEIGHT_CM - (BEAM_HEIGHT_CM + WEATHER_BOARD_HEIGHT_CM + 10);
        double measureLineBack = CARPORT_TOP_HEIGHT_CM - (BEAM_HEIGHT_CM + WEATHER_BOARD_HEIGHT_CM + 20);

        carportInnerSvg.addLine(0,yPositionBottom,carport.getLength(),CARPORT_TOP_HEIGHT_CM, BASE_STYLE);
        carportInnerSvg.addLine(0,yPositionBottom, 0, measureLineFront, BASE_STYLE);
        carportInnerSvg.addLine(carport.getLength(), yPositionBottom, carport.getLength(), measureLineBack, BASE_STYLE);
    }

    private void addRafters()
    {
        double rafterWidth = 4.5;
        double rafterHeight = BEAM_HEIGHT_CM;

        RafterCalculationDTO rafterCalcDTO = PartCalculator.calculateRafters(carport.getLength(), rafterWidth);
        int numberOfRafters = rafterCalcDTO.numberOfRafters();
        double spacing = rafterCalcDTO.spacing();
        double middlePoint = (rafterHeight + WEATHER_BOARD_HEIGHT_CM) / 2;
        double currentXPos = middlePoint;

        for(int i = 0; i < numberOfRafters ; i++)
        {
            carportInnerSvg.addRectangle(currentXPos, middlePoint, rafterHeight, rafterWidth, BASE_STYLE);
            currentXPos += spacing;
        }
    }


    private void addPost()
    {
        POST_BACK_PLACEMENT_CM = carport.getLength() - 30.0;

        int totalNumberOfPost = PartCalculator.calculateNumberOfPostsWithOutShed(carport.getLength());
        int numberOfPostsPerRow = totalNumberOfPost / 2;

        carportInnerSvg.addRectangle(POST_FRONT_PLACEMENT_CM, yPositionBottom - POST_HEIGHT_CM, POST_HEIGHT_CM, POST_WIDTH_CM, BASE_STYLE);
        carportInnerSvg.addRectangle(POST_BACK_PLACEMENT_CM, yPositionBottom - POST_HEIGHT_CM, POST_HEIGHT_CM, POST_WIDTH_CM, BASE_STYLE);

        if(numberOfPostsPerRow == 3)
        {
            carportInnerSvg.addRectangle(POST_CENTER_PLACEMENT_CM, yPositionBottom - POST_HEIGHT_CM, POST_HEIGHT_CM, POST_WIDTH_CM, BASE_STYLE);
        }
    }

    private void addBeamAndWeatherBoard()
    {
        double dropInCm = 10;
        double spaceBetween = 3;
        double overhang = 4;

        double weatherBoardX1 = 0.0;
        double weatherBoardX2 = carport.getLength();
        double weatherBoardY1 = yPositionTop;
        double weatherBoardY2 = yPositionTop + dropInCm;

        double beamX1 = 0.0 + overhang;
        double beamX2 = carport.getLength() - overhang;
        double beamY1 = yPositionTop + WEATHER_BOARD_HEIGHT_CM + spaceBetween;
        double beamY2 = yPositionTop + WEATHER_BOARD_HEIGHT_CM + spaceBetween + dropInCm;

        addBeamOrWeatherBoard(weatherBoardX1, weatherBoardY1, weatherBoardX2, weatherBoardY2, WEATHER_BOARD_HEIGHT_CM); // Weather board
        addBeamOrWeatherBoard(beamX1, beamY1, beamX2, beamY2, BEAM_HEIGHT_CM); // Beam
    }

    private void addArrows()
    {
        double tickLength = 20;
        double xStartOffSet = 4.5 / 2;
        double innerArrowTopY = INNER_SVG_Y_START + CARPORT__HEIGHT_OFFSET_TO_TOP_BEAM;

        RafterCalculationDTO rafterCalculationDTO = PartCalculator.calculateRafters(carport.getLength(),4.5);
        int numberOfSpaces = rafterCalculationDTO.numberOfRafters() - 1;
        double spacing = rafterCalculationDTO.spacing();

        for(int i = 0; i < numberOfSpaces; i++)
        {
            double x1 = INNER_SVG_X_START + (i * spacing + xStartOffSet);
            double x2 = INNER_SVG_X_START + ((i + 1) * spacing + xStartOffSet);
            double y = arrowYTopMargin;

            carportSideSvg.addLineWithArrows(x1, y, x2, y);
            carportSideSvg.addLine(x1, y - tickLength/2, x1, y + tickLength/2, BASE_STYLE);
            carportSideSvg.addLine(x2, y - tickLength/2, x2, y + tickLength/2, BASE_STYLE);

            double midX = (x1 + x2) / 2.0;
            double spacing_meters = spacing / 100.0;
            carportSideSvg.addText(midX, y - 15, 0, String.format("%.2f", spacing_meters));
        }

        // Left arrow
        carportSideSvg.addLineWithArrows(arrowLeftXStart, INNER_SVG_Y_START, arrowLeftXStart, arrowBottomY);
        //Top measure line
        carportSideSvg.addLine(arrowLeftXStart - tickLength, INNER_SVG_Y_START, arrowInnerLeftXStart + tickLength, INNER_SVG_Y_START, BASE_STYLE);
        //Bottom measure line
        carportSideSvg.addLine(arrowLeftXStart - tickLength, arrowBottomY, arrowInnerLeftXStart + tickLength, arrowBottomY, BASE_STYLE);

        //Left inner arrow
        carportSideSvg.addLineWithArrows(arrowInnerLeftXStart, innerArrowTopY, arrowInnerLeftXStart, arrowBottomY);
        //Left inner measure line
        carportSideSvg.addLine(arrowInnerLeftXStart - tickLength, innerArrowTopY, arrowInnerLeftXStart + tickLength, innerArrowTopY, BASE_STYLE);

        // Bottom arrow
        carportSideSvg.addLineWithArrows(INNER_SVG_X_START, arrowYBottomMargin, arrowBottomXEnd, arrowYBottomMargin);
    }

    private void addArrowText()
    {
        int carportHeightToBeamBottom = 20;
        int innerArrowLength = CARPORT_TOP_HEIGHT_CM - carportHeightToBeamBottom;

        double carportHeightInMeters = CARPORT_TOP_HEIGHT_CM / 100.0;
        double carportHeightToBeamBottomInMeters = innerArrowLength / 100.0;

        //Left arrow
        carportSideSvg.addText(arrowLeftXStart - 10, arrowBottomY / 2, -90, String.valueOf(carportHeightInMeters));

        //Left inner arrow
        carportSideSvg.addText(INNER_SVG_X_START * 0.75 - 10, arrowBottomY / 2, -90, String.valueOf(carportHeightToBeamBottomInMeters));

        //Bottom arrow
        carportSideSvg.addText(arrowBottomXEnd / 2, arrowYBottomMargin + 15, 0, String.valueOf(carport.getLength()));

    }

    private void addBeamOrWeatherBoard(double x1, double y1, double x2, double y2, double offset)
    {
            String points = String.format("%.1f,%.1f %.1f,%.1f %.1f,%.1f %.1f,%.1f",
                    x1, y1,             // Top left
                    x2, y2,             // Top right
                    x2, y2 + offset,    // Bottom right
                    x1, y1 + offset);   // Bottom left

            carportInnerSvg.addPolygon(points, BASE_STYLE);
    }

    private String getInnerViewBox(int height, int length)
    {
        String heightString = String.valueOf(CARPORT_TOP_HEIGHT_CM);
        String lengthString = String.valueOf(length);

        return "0 0 " + lengthString + " " + heightString;
    }
    @Override
    public String toString() {
        return carportSideSvg.toString();
    }
}