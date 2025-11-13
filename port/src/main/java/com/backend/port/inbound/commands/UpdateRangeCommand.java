package com.backend.port.inbound.commands;

/**
 * Command object describing a request to change a user's notification radius.
 *
 * @param firebaseUid Firebase identifier of the user whose range should be updated
 * @param rangeKm new range value expressed in kilometers
 */
public record UpdateRangeCommand(String firebaseUid, int rangeKm) {

}
