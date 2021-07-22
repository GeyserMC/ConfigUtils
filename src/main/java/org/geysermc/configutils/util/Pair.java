package org.geysermc.configutils.util;

public class Pair<X, Y> {
  private final X x;
  private final Y y;

  public Pair(X x, Y y) {
    this.x = x;
    this.y = y;
  }

  public X x() {
    return x;
  }

  public Y y() {
    return y;
  }

  @Override
  public String toString() {
    return "Pair{" +
        "x=" + x +
        ", y=" + y +
        '}';
  }
}
