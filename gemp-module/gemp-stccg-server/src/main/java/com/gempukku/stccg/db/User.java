package com.gempukku.stccg.db;

import com.gempukku.stccg.DBData;
import com.gempukku.stccg.async.HttpProcessingException;
import com.mysql.cj.util.StringUtils;

import java.net.HttpURLConnection;
import java.util.*;

public class User {

    public enum Type {
        ADMIN("a"),
        LEAGUE_ADMIN("l"),
        PLAY_TESTER("p"),
        UNBANNED("n"),
        USER("u");

        private final String _value;

        Type(String value) {
            _value = value;
        }

        public String getValue() {
            return _value;
        }

        public String toString() {
            return getValue();
        }

        public static List<Type> getTypes(String typeString) {
            List<Type> types = new ArrayList<>();
            for (Type type : values()) {
                if (typeString.contains(type.getValue())) {
                    types.add(type);
                }
            }
            return types;
        }

    }

    private final int _id;
    private final String _name;
    private final String _password;
    private final String _type;
    private Integer _lastLoginReward;
    private final Date _bannedUntil;
    private final String _createIp;
    private final String _lastIp;

    public User(DBData.DBPlayer def) {
        this(def.id, def.name, def.password, def.type, def.last_login_reward, def.GetBannedUntilDate(), def.create_ip, def.last_ip);
    }

    public User(int id, String name, String password, String type, Integer lastLoginReward, Date bannedUntil, String createIp, String lastIp) {
        _id = id;
        _name = name;
        _password = password;
        _type = type;
        _lastLoginReward = lastLoginReward;
        _bannedUntil = bannedUntil;
        _createIp = createIp;
        _lastIp = lastIp;
    }

    public int getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }

    public String getPassword() {
        return _password;
    }

    public String getType() {
        return _type;
    }

    public boolean hasType(Type type) {
        return Type.getTypes(_type).contains(type);
    }

    public Integer getLastLoginReward() {
        return _lastLoginReward;
    }

    public void setLastLoginReward(int lastLoginReward) {
        _lastLoginReward = lastLoginReward;
    }

    public Date getBannedUntil() {
        return _bannedUntil;
    }

    public String getCreateIp() {
        return _createIp;
    }

    public String getLastIp() {
        return _lastIp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User player = (User) o;

        return Objects.equals(_name, player._name);
    }

    @Override
    public int hashCode() {
        return _name != null ? _name.hashCode() : 0;
    }

    public PlayerInfo GetUserInfo() {
        return new PlayerInfo(_name, _type);
    }

    @SuppressWarnings("unused") // Class members accessed through JSON, which may not be obvious to IDE
    public static class PlayerInfo {
        public final String name;
        public final String type;

        public PlayerInfo(String name, String info) {
            this.name = name;
            type = info;
        }
    }

    private boolean isType(User.Type type) {
        return _type.contains(type.getValue());
    }

    private boolean hasNoPassword() {
        return StringUtils.isNullOrEmpty(_password);
    }

    public void checkLogin() throws HttpProcessingException {
        if (hasNoPassword()) {
            throw new HttpProcessingException(HttpURLConnection.HTTP_ACCEPTED); // 202
        }
        if (isType(User.Type.USER)) {
            final Date bannedUntil = getBannedUntil();
            if (bannedUntil != null && bannedUntil.after(new Date()))
                throw new HttpProcessingException(HttpURLConnection.HTTP_CONFLICT); // 409
        } else {
            throw new HttpProcessingException(HttpURLConnection.HTTP_FORBIDDEN); // 403
        }
    }

}