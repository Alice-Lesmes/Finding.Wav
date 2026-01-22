package com.example.findingwav.data

/**
 * Vars/Vals to define behaviour in the app.
 * Essentially just constants
 */
/**
 * How long a song needed to play for before being considered to be added.
 *
 * <b>EXAMPLE:</b> `NECESSARY_PLAYTIME (0.9) * player.currentPosition`
 *  90% has been played, therefore we can add!
 */
public var NECESSARY_PLAYTIME : Double = 0.9;

/**
 * Whether adding multiple of the same song is allowed in the playlist
 */
public var REPEAT_SONGS : Boolean = false

public var DEBUG : Boolean = true