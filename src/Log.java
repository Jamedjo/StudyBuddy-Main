public class Log {
    LogType defaultLog = LogType.Debug;
    boolean showDebug;

    Log(){
        showDebug = true;//set false when releasing
    }
    Log(boolean isDebug){
        showDebug = isDebug;
    }

    void print(Object message){
        print(defaultLog, message);
    }
    void print(Object message, LogType type){
        print(type,message);
    }
    void print(LogType type, Object message){
        String msg = message.toString();
        switch(type){
            case DebugError:
                if(!showDebug) return;
            case Error:
                System.err.println(message);
                break;
            case Debug:
                if(!showDebug) return;
            default:
                System.out.println(message);
        }
    }
}

enum LogType{
    Plain,
    Debug,
    Error,
    DebugError,
    Log,
    Status,
    PopupError,
    Popup,
    Title;//exception,clear,question?
}