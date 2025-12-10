package app.util;

import app.entities.Carport;
import app.entities.Shed;
import app.enums.ShedPlacement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PostPlacementCalculatorUtil
{
    private static final double POST_FRONT_PLACEMENT_CM = 100.0;
    private static final double POST_BACK_EDGE_INSET_CM = 30.0;
    private static final double MAX_DISTANCE_BETWEEN_POSTS = 310.0;
    private static final double POST_WIDTH_CM = 10.0;
    private static final double POST_SIDE_EDGE_INSET_CM = 35.00;

    public static List<Double> calculatePostPlacements(Carport carport)
    {
        // Is Carport with 2 or 3 posts (per side)
        // Is Carport with shed
        // Is shed full width
        // Is shedpost placement further than center post (back to front)
        // Arrow points to shed post first, then centerpost
        // Or is shedpost equal to centerpost
        // Arrow only points to centerpost
        // Else shedpost is not further than centerpost
        // Arrow points to centerpost first, then shedpost
        // Else shed is half width (LEFT)
        // Not handled - include in report

        //POST ARROWS Second arrow
        // If (carport > ? ) 6 posts

        // if shed != null
        // Case 1: shed is equal to center post (back to front)
        // Case 2: shed is shorter that center post (back to front)
        // Case 3: shed is longer than center post (back to front)


        List<Double> postPlacements = new ArrayList<>();

        postPlacements.add(POST_FRONT_PLACEMENT_CM);
        double backPostPlacement = carport.getLength() - POST_BACK_EDGE_INSET_CM;

        Shed shed = carport.getShed();
        int totalPosts = PartCalculator.calculateNumberOfPostsWithOutShed(carport.getLength());
        int postPrRow = totalPosts / 2;

        if (shed == null)
        {
            if(postPrRow == 3)
            {
                double middleCenterPlacement = calculateCenterPostPlacement(carport);
                postPlacements.add(middleCenterPlacement);
            }
        }
        else
        {
            if(postPrRow == 3)
            {
                double middleCenterPlacement = calculateCenterPostPlacement(carport);
                postPlacements.add(middleCenterPlacement);
            }
            double shedPostPlacement = carport.getLength() - (shed.getLength() + POST_BACK_EDGE_INSET_CM);
            postPlacements.add(shedPostPlacement);
        }

        postPlacements.add(backPostPlacement);
        Collections.sort(postPlacements);

        return postPlacements;
    }

    public static double calculatePostYPosition(Carport carport)
    {
        double postYPosition = 0.0;

        if(carport.getShed() == null){ return postYPosition;}

        ShedPlacement placement = carport.getShed().getShedPlacement();

        switch (placement)
        {
            case LEFT -> postYPosition = calculateLeftShedYPositions(carport);

            case FULL_WIDTH -> postYPosition = calculateShedMiddleYPosition(carport);

            default -> postYPosition = postYPosition;
        }

        return postYPosition;
    }

    public static int calculateCenterPostPlacement(Carport carport)
    {
        final int MIN_DISTANCE_BETWEEN_POSTS = 100;
        double postCenterPlacementCm = POST_FRONT_PLACEMENT_CM + MAX_DISTANCE_BETWEEN_POSTS;

        if(carport.getShed() == null)
        {
            return (int) postCenterPlacementCm;
        }

        double postShedPlacementCm = calculateShedPostPlacement(carport);
        double spaceBetweenCenterAndShedPost = postShedPlacementCm - postCenterPlacementCm;
        boolean isPostFurtherThanCenterPost = false;

        if (spaceBetweenCenterAndShedPost < 0)
        {
            spaceBetweenCenterAndShedPost = Math.abs(spaceBetweenCenterAndShedPost);
            isPostFurtherThanCenterPost = true;
        }

        if (spaceBetweenCenterAndShedPost == 0)
        {
            return (int) postCenterPlacementCm;
        }
        else if (spaceBetweenCenterAndShedPost < MIN_DISTANCE_BETWEEN_POSTS)
        {
            if (isPostFurtherThanCenterPost)
            {
                postCenterPlacementCm = postShedPlacementCm + MIN_DISTANCE_BETWEEN_POSTS + POST_WIDTH_CM;
            }
            else
            {
                postCenterPlacementCm = postShedPlacementCm - MIN_DISTANCE_BETWEEN_POSTS;
            }
        }

        return (int) postCenterPlacementCm;
    }

    public static int calculateShedPostPlacement(Carport carport)
    {
        return (int) (carport.getLength() - (carport.getShed().getLength() + POST_BACK_EDGE_INSET_CM));
    }

    private static double calculateLeftShedYPositions(Carport carport)
    {
        double sides = 2;
        double totalCarportPostInsetLength = POST_SIDE_EDGE_INSET_CM * sides;
        double innerCarportLength = carport.getShed().getWidth() - totalCarportPostInsetLength;
        double shedCenterYPosition = innerCarportLength / 2.0;

        return shedCenterYPosition;
    }

    private static double calculateShedMiddleYPosition(Carport carport)
    {
        double sides = 2;
        double totalCarportPostInsetLength = POST_SIDE_EDGE_INSET_CM * sides;
        double innerCarportLength = carport.getWidth() - totalCarportPostInsetLength;
        double shedCenterYPosition = (innerCarportLength / 2.0) - POST_WIDTH_CM;

        return shedCenterYPosition;
    }
}
