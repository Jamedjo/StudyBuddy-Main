public class Log {
    LogType defaultLog = LogType.Debug;
    boolean showDebug;

    Log(){
        showDebug = false;//set false when releasing
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
    static void Print(LogType type,Object message){
        Log l = new Log();
        l.print(type, message);
    }
    void print(LogType type, Object message){
        //String msg = message.toString();
        switch(type){
            case DebugError:
                if(!showDebug) return;//comment out to force all errors to be shown
            case Error:
                System.err.println(message);
                break;
            case Debug:
                if(!showDebug) return;//comment out to force all non-error messages to be shown
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