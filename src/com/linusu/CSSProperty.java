package com.linusu;

public enum CSSProperty {
    
    OPACITY,
    
    BORDER_RADIUS,
    BOX_SHADOW,
    
    USER_SELECT,
    BOX_SIZING,
    
    BACKGROUND_SIZE,
    BACKGROUND_CLIP,
    BACKGROUND_ORIGIN,
    
    TRANSITION,
    TRANSITION_DURATION,
    TRANSITION_PROPERTY,
    TRANSITION_DELAY,
    TRANSITION_TIMING_FUNCTION,
    
    COLUMN_COUNT,
    COLUMN_FILL,
    COLUMN_GAP,
    COLUMN_RULE,
    COLUMN_SPAN,
    COLUMN_WIDTH,
    
    UNKNOWN;
    
    @Override
    public String toString() {
        return super.toString().toLowerCase().replace('_', '-');
    }
    
    public static CSSProperty fromString(String string) {
        try {
            return Enum.valueOf(CSSProperty.class, string.toUpperCase().replace('-', '_'));
        } catch(IllegalArgumentException e) {
            return Enum.valueOf(CSSProperty.class, "UNKNOWN");
        }
    }
    
}
