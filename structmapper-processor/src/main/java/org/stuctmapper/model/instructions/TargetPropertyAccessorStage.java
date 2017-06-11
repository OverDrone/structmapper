package org.stuctmapper.model.instructions;

public enum TargetPropertyAccessorStage {
    /**
     * До проверки значения свойства. Можно объявить переменные
     */
    DECLARE,

    /**
     * if не требуется, данное свойство не может быть null 
     */
    NOT_NULL,
    
    /**
     * Внутри блока if (var != null) 
     */
    IF_NOT_NULL,
    
    /**
     * Внутри блока else
     */
    IF_NULL,
    
    /**
     * После проверки
     */
    FINISH
}
