package com.ruchir.InventoryStore.Service;

import com.ruchir.InventoryStore.model.UserEntity;

public interface UserService {
    UserEntity registerUser(String username, String password);
    UserEntity findByUsername(String username);
}
