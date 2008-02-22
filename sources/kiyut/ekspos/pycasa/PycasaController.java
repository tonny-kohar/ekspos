package kiyut.ekspos.pycasa;

import pycasa.controller.Controller;

/**
 * PycasaController
 * Overriden to overwrite quit, so it does not quit the whole application, 
 * but close its window instead.
 * 
 * Overriden login, to set flag connected
 * 
 *
 */
public class PycasaController extends Controller {

    @Override
    public void quit() {
        PycasaManager.getInstance().closeManageWindow();
    }
    
    @Override
    public boolean login(String user, String password, String proxy, String port) {
        boolean connected = super.login(user,password, proxy,port);
        
        PycasaManager.getInstance().setConnected(connected);
        
        return connected;
    }
}
