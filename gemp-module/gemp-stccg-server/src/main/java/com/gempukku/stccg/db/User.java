package com.gempukku.stccg.db;

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
        this(def.id, def.name, def.password, def.type, def.last_login_reward, def.GetBannedUntilDate(), def.create_ip,
                def.last_ip);
    }

    public User(int id, String name, String password, String type, Integer lastLoginReward, Date bannedUntil,
                String createIp, String lastIp) {
        _id = id;
        _name = name;
        _password = password;
        _type = type;
        _lastLoginReward = lastLoginReward;
        _bannedUntil = bannedUntil;
        _createIp = createIp;
        _lastIp = lastIp;
    }

    public final int getId() {
        return _id;
    }

    public final String getName() {
        return _name;
    }

    public final String getPassword() {
        return _password;
    }

    public final String getType() {
        return _type;
    }

    public final boolean hasType(Type type) {
        Collection<Type> types = new ArrayList<>();
        for (Type typeItr : Type.values()) {
            if (_type.contains(typeItr.getValue())) {
                types.add(typeItr);
            }
        }
        return types.contains(type);
    }

    public final Integer getLastLoginReward() {
        return _lastLoginReward;
    }

    public final void setLastLoginReward(int lastLoginReward) {
        _lastLoginReward = lastLoginReward;
    }

    public final Date getBannedUntil() {
        return _bannedUntil;
    }

    public final String getCreateIp() {
        return _createIp;
    }

    public final String getLastIp() {
        return _lastIp;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        User player = (User) obj;

        return Objects.equals(_name, player._name);
    }

    @Override
    public final int hashCode() {
        return _name != null ? _name.hashCode() : 0;
    }

    public final PlayerInfo GetUserInfo() {
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
        String typeValue = type.getValue();
        return _type.contains(typeValue);
    }

    private boolean hasNoPassword() {
        return StringUtils.isNullOrEmpty(_password);
    }

    public final void checkLogin() throws HttpProcessingException {
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

    public final void banIpPrefix(IpBanDAO ipBanDAO) {
        final String lastIp = _lastIp;
        int finalPeriodIndex = lastIp.lastIndexOf('.');
        String lastIpPrefix = lastIp.substring(0, finalPeriodIndex + 1);
        ipBanDAO.addIpPrefixBan(lastIpPrefix);
    }
}