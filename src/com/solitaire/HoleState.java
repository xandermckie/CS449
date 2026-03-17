package com.solitaire;

/**
 * Represents the state of a single hole on the Peg Solitaire board.
 */
public enum HoleState {
    /** Not part of this board shape. */
    INVALID,
    /** A valid hole with no peg in it. */
    EMPTY,
    /** A valid hole containing a peg. */
    PEG
}