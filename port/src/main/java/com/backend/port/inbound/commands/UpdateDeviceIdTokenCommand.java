package com.backend.port.inbound.commands;

/**
 * Command that requests storing or refreshing the device identifier/FCM token for a user.
 *
 * @param firebaseUid Firebase identifier of the user
 * @param deviceIdToken latest device token reported by the client
 */
public record UpdateDeviceIdTokenCommand(String firebaseUid, String deviceIdToken) {

}
