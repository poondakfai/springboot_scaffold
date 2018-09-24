package org.poondakfai.prototype.scaffold.model;


public enum Roles {
  UNKNOWN(0),
  SYSTEM(1),   // This role is locally used to manage Oauth Server accounts only
  CFG_USER(2), // This role could grant oauth admin scope and all other scopes
  EX_USER(3),  // This role could grant oauth advance scope and standard scope
  USER(4);     // This role could grant oauth standard scope


  private static final String AUTH_PREFIX;
  private static final Roles[] IDS_MAP;
  private static final String[] NAMES_MAP;


  private final int id;


  Roles(int id) {
    this.id = id;
  }

  public int getId() {
    return this.id;
  }

  @Override
  public String toString() {
    return NAMES_MAP[this.getId()];
  }

  public String getAuthority() {
    return AUTH_PREFIX + this.toString();
  }


  public static Roles getRole(int id) {
    if (id <= 0 || id >= IDS_MAP.length) {
      return UNKNOWN;
    }
    return IDS_MAP[id];
  }

  static {
    // Prefix to convert role string to authority string as Spring security does
    AUTH_PREFIX = "ROLE_";
    // Retrieves the list of enum constants defined by the enum in the order
    // they're declared. Below is document reference link:
    // https://docs.oracle.com/javase/tutorial/reflect/special/enumMembers.html
    IDS_MAP = Roles.class.getEnumConstants();
    // This map convert id to name which is computed by security filter
    NAMES_MAP = new String[] {
      "",
      "SYSTEM",
      "CFG_USER",
      "EX_USER",
      "USER",
    };
  }
}


