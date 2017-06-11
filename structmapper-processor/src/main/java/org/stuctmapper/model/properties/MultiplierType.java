package org.stuctmapper.model.properties;

public enum MultiplierType {
    /**
     * Выражение не предусматривает множественность
     */
    ABSENT,

    /**
     * Выражение - это объект {@link java.util.Map Map}.
     * Перечисляются все ключи
     */
    MAP,
    /**
     * Перечисляются все элементы enum
     */
    ENUM,
    
    /**
     * Выражение допускает перечисление, но список неизвестен (например параметр метода)
     */
    UNKNOWN
}
