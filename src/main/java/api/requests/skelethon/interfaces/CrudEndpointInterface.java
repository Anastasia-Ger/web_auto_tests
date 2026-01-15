package api.requests.skelethon.interfaces;

import api.models.BaseModel;

public interface CrudEndpointInterface {
    Object post(BaseModel model);
    Object get();
    Object get(int id);
    Object update(BaseModel model);
    Object delete(int id);
}
