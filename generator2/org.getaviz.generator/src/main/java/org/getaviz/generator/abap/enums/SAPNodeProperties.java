package org.getaviz.generator.abap.enums;

public enum SAPNodeProperties {

    // alle Elemente
    element_id, object_name, type, type_name, creator, created, changed_by, changed, iteration,

    //spezifische Eigenschaften
    datatype, rowtype, length, decimals, domname, dataelement, structure, modifiers, number_of_statements,
    local_class, container_id, uses_id,

    //
    migration_findings,

    //Debugger
    position, param_type
}
