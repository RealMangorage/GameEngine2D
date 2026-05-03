package org.mangorage.game.world.misc;

public interface INode {
    // How many things can send items TO this node
    int getMaxInputs();
    // How many things this node can send items TO
    int getMaxOutputs();

    // Current connection counts
    int getInputCount();
    int getOutputCount();

    default boolean canAcceptMoreInputs() {
        return getInputCount() < getMaxInputs();
    }

    default boolean canAddMoreOutputs() {
        return getOutputCount() < getMaxOutputs();
    }

    boolean connect(INode node);

    default void registerInput() {

    }
}