package org.processmining.streambasedeventlog.models;

import java.util.Collection;
import java.util.Map;

/**
 * idea of payload, map a *value* to a collection of case identifiers.
 * 
 * @author svzelst
 *
 * @param <T>
 *            type of the data
 * @param <S>
 *            type of the meta data
 * 
 *            svzelst@20170720: removed generics (... for now)
 */
public interface Payload extends Map<String, Collection<String>> {

}
