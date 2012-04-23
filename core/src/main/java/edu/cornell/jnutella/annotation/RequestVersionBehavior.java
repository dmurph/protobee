package edu.cornell.jnutella.annotation;

public enum RequestVersionBehavior {
	/**
	 * The protocol actively requests this capability
	 */
	ACTIVE,
	/**
	 * The protocol does not actively request he capability, but if it's present
	 * then the module is loaded.
	 */
	SILENT
}
