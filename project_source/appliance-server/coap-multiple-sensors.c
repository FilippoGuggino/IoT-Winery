#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <random.h>
#include <time.h>
#include "contiki.h"
#include "contiki-net.h"
#include "coap-engine.h"
#include "coap-blocking-api.h"

#include "pt.h"

/* Log configuration */
#include "coap-log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL  LOG_LEVEL_APP

/* Server Address */
#define SERVER_EP "coap://[fd00::1]:5683"
#define TOGGLE_INTERVAL 5
#define SETUP_TIME 2
#define BUFFER_SIZE 200

static struct etimer et;
static struct etimer subscribe_timer;


#if PLATFORM_HAS_LIGHT
     #include "dev/light-sensor.h"
     extern coap_resource_t res_light;
#endif
#if PLATFORM_HAS_TEMPERATURE
#include "dev/temperature-sensor.h"
     extern coap_resource_t res_temperature;
#endif
#ifdef TEST
     extern coap_resource_t  res_temperature;
     extern coap_resource_t  res_light;
     extern coap_resource_t  res_ac;
#endif

PROCESS(multiple_sensors_process, "Winery sensor device main process");
PROCESS(subscribe_to_server, "Subscription process");
AUTOSTART_PROCESSES(&subscribe_to_server);

/* leading and ending slashes only for demo purposes, get cropped automatically when setting the Uri-Path */
char *registration_url = "/devices";
char *resource_url = "sensors/temperature";
char *device_role = "sensor_node";

/* This function is will be passed to COAP_BLOCKING_REQUEST() to handle responses. */
void client_chunk_handler(coap_message_t *response){
     const unsigned char *chunk;

     if(response == NULL) {
          puts("Request timed out");
          return;
     }

     int len = coap_get_payload(response, &chunk);

     if(strcoll((char*)chunk, "ok") != 0){
          LOG_ERR("error has occurred");
     }

     printf("|%.*s", len, (char *)chunk);
}

// Process used at startup to subscribe to the server
PROCESS_THREAD(subscribe_to_server, ev, data)
{
     static coap_endpoint_t server_ep;
     static coap_message_t request[1];      /* This way the packet can be treated as pointer as usual. */
     static bool flag = true;

     PROCESS_BEGIN();

#if PLATFORM_HAS_LIGHT
     coap_activate_resource(&res_light, "sensors/light");
     SENSORS_ACTIVATE(light_sensor);
#endif
#if PLATFORM_HAS_TEMPERATURE
     coap_activate_resource(&res_temperature, "sensors/temperature");
     SENSORS_ACTIVATE(temperature_sensor);
#endif

#ifdef TEST
     coap_activate_resource(&res_temperature, "sensors/temperature");
     coap_activate_resource(&res_light, "sensors/light");
     coap_activate_resource(&res_ac, "actuators/ac");
#endif

     etimer_set(&subscribe_timer, SETUP_TIME * CLOCK_SECOND);

     while(flag){
          PROCESS_YIELD();

          if(etimer_expired(&subscribe_timer)) {
               printf("\n--Start device subscription--\n");
               coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);

               /* prepare request, TID is set by COAP_BLOCKING_REQUEST() */
               coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);
               coap_set_header_uri_path(request, registration_url);

               static char buff[BUFFER_SIZE];
               snprintf(buff, BUFFER_SIZE, "{\"device_role\": \"%s\"}", device_role);

               coap_set_payload(request, (uint8_t *)buff, strlen(buff));

               COAP_BLOCKING_REQUEST(&server_ep, request, client_chunk_handler);
               printf("\n--Done--\n");

               flag = false;

               process_start(&multiple_sensors_process, NULL);
          }
     }

     PROCESS_END();
}

// every TOGGLE_INTERVAL simulate a new value coming from the sensors, then notify observers
PROCESS_THREAD(multiple_sensors_process, ev, data){

     PROCESS_BEGIN();

     etimer_set(&et, TOGGLE_INTERVAL * CLOCK_SECOND);

     while(1) {
          PROCESS_YIELD();

          // every TOGGLE_INTERVAL simulate a new value coming from the sensor, then notify observers
          if(etimer_expired(&et)) {
               printf("--Toggle timer--\n");

               res_temperature.trigger();
               res_light.trigger();

               etimer_reset(&et);
          }
          else {
               printf("--Ho visto cose--\n");
          }
     }

     PROCESS_END();
}
