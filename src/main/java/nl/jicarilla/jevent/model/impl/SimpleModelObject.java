package nl.jicarilla.jevent.model.impl;

import java.io.Serializable;

public class SimpleModelObject implements Serializable {
    private boolean isStored = false;
    private boolean isModified = false;
    private boolean isDeleted = false;

    protected SimpleStore store;

    public SimpleModelObject(SimpleStore store) {
        this.store = store;
    }

    public void save() {
        if(isDeleted) { throw new RuntimeException("This object has been deleted"); }
        if(!isStored) { doCreate(); isStored = true; isModified = false; }
        else if(isModified) { doUpdate(); isModified = false; }
    }
    
    public void delete() {
        if(isStored) { doDelete(); isDeleted = true; }
    }

    protected void doCreate() {
        store.create(this);
    }
    protected void doUpdate() {
        store.update(this);
    }
    protected void doDelete() {
        store.delete(this);
    }
}
