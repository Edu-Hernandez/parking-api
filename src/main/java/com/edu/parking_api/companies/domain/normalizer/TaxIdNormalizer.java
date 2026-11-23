package com.edu.parking_api.companies.domain.normalizer;

public final class TaxIdNormalizer {

    private TaxIdNormalizer() {}

    /**
     * Recorta el identificador fiscal. Un valor nulo o en blanco devuelve null,
     * porque el índice único de tax_id solo aplica a valores no nulos y un
     * texto vacío colisionaría entre empresas.
     */
    public static String normalize(String taxId) {
        if (taxId == null) {
            return null;
        }
        String trimmed = taxId.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
