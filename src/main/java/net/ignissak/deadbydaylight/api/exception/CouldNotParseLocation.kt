package net.ignissak.deadbydaylight.api.exception

/**
 * This exception is thrown when plugin could not parse location
 * from configuration file. Proper format is world;x;y;z;yaw;pitch.
 */
class CouldNotParseLocation(m: String) : Exception(m)