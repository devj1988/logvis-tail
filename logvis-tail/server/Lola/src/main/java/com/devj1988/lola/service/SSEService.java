package com.devj1988.lola.service;

import com.devj1988.lola.model.SyslogMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class SSEService {

    private Map<String, CopyOnWriteArrayList<SseEmitter>> emittersMap = new ConcurrentHashMap<>();

    private ExecutorService sseMvcExecutor = Executors.newCachedThreadPool();

    public SseEmitter getEmitterForApplication(String application) {
        SseEmitter emitter = new SseEmitter(-1L);
        if (!emittersMap.containsKey(application)) {
            synchronized (this) {
                if (!emittersMap.containsKey(application)) {
                    emittersMap.put(application, new CopyOnWriteArrayList<SseEmitter>());
                }
            }
        }
        CopyOnWriteArrayList<SseEmitter> list = emittersMap.get(application);
        list.add(emitter);

        emitter.onCompletion(()->{
            list.remove(emitter);
        });
        emitter.onTimeout(()-> {
            list.remove(emitter);
        });
        emitter.onError((e) -> {
            list.remove(emitter);
        });

        return emitter;
    }

    public void removeEmitterForApplication(SseEmitter emitter, String application) {
        CopyOnWriteArrayList<SseEmitter> list = emittersMap.get(application);
        if (list != null) {
            list.remove(emitter);
        }
    }

    public void newSysLogMessage(SyslogMessage syslogMessage) {
        CopyOnWriteArrayList<SseEmitter> list = emittersMap.get(syslogMessage.getApplication());
        if (list != null && !list.isEmpty()) {
            String message = syslogMessage.getSource() + " : " + syslogMessage.getLog();
            list.forEach(emitter -> sendMessage(emitter, message, list));
        }
    }

    private void sendMessage(SseEmitter emitter, String message, CopyOnWriteArrayList<SseEmitter> list) {
        sseMvcExecutor.execute(() -> {
            try {
                SseEmitter.SseEventBuilder event = SseEmitter.event()
                        .data(message)
                        .id(String.valueOf(message.hashCode()))
                        .name("new log msg");
                emitter.send(event);
            } catch (Exception ex) {
                log.info("error sending event", ex);
                emitter.completeWithError(ex);
            }
        });
    }
}
