package model.world;

public enum CellType {
    PATH(false, false, "#333333"), 
    HOUSE(true, true, "#9999ff"), 
    SHOP(true, true, "#99ffff"), 
    TREE(true, false, "#99ff99"), 
    LAMP(false, false, "#ffff99");

    public final boolean obstructsMovement;
    public final boolean obstructsView;
    public final String colour;

    public static CellType[] types = { PATH, HOUSE, SHOP, TREE, LAMP };

    private CellType(boolean obstructsMovement, boolean obstructsView, String colour) {
        this.obstructsMovement = obstructsMovement;
        this.obstructsView = obstructsView;
        this.colour = colour;
    }
}
