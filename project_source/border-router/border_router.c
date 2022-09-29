#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "contiki-net.h"
#include "coap-engine.h"
#include "coap-blocking-api.h"

#include "pt.h"

#include "coap-log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_APP

/* Server Address */
#define SERVER_EP "coap://[fd00::1]:5683"
#define TOGGLE_INTERVAL 2
#define SETUP_TIME 1
#define BUFFER_SIZE 200

PROCESS(cluster_info_process, "Cluster info main process");
PROCESS(subscribe_to_server, "Subscription process");
PROCESS(webserver, "Webserver process");
AUTOSTART_PROCESSES(&subscribe_to_server, &webserver);

static struct etimer et;
static struct etimer subscribe_timer;
extern coap_resource_t res_cluster;

/* leading and ending slashes only for demo purposes, get cropped automatically when setting the Uri-Path */
char *registration_url = "/devices";
char *resource_url = "cluster_devices";
char *device_role = "border_router";

/* This function is will be passed to COAP_BLOCKING_REQUEST() to handle responses. */
void client_chunk_handler(coap_message_t *response){
     const uint8_t *chunk;

     if(response == NULL) {
          puts("Request timed out");
          return;
     }

     int len = coap_get_payload(response, &chunk);

     printf("|%.*s", len, (char *)chunk);
}

// Process used at startup to subscribe to the server
PROCESS_THREAD(subscribe_to_server, ev, data)
{
     static coap_endpoint_t server_ep;
     static coap_message_t request[1];      /* This way the packet can be treated as pointer as usual. */
     static bool flag = true;

     PROCESS_BEGIN();

     coap_activate_resource(&res_cluster, resource_url);

     etimer_set(&subscribe_timer, SETUP_TIME * CLOCK_SECOND);

     while(flag) {
          PROCESS_YIELD();

          if(etimer_expired(&subscribe_timer)) {
               printf("--Start BR subscription--\n");

               coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);

               /* prepare request, TID is set by COAP_BLOCKING_REQUEST() */
               coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);
               coap_set_header_uri_path(request, registration_url);

               static char buff[BUFFER_SIZE];
               snprintf(buff, BUFFER_SIZE, "{\"device_role\": \"%s\"}", device_role);

               coap_set_payload(request, (uint8_t *)buff, strlen(buff));

               COAP_BLOCKING_REQUEST(&server_ep, request, client_chunk_handler);

               printf("--Done--\n");

               flag = false;

               process_start(&cluster_info_process, NULL);
          }
          else {
               printf("--Ho visto cose--\n");
          }
     }


     PROCESS_END();
}

PROCESS_THREAD(webserver, ev, data){
     PROCESS_BEGIN();
     #if BORDER_ROUTER_CONF_WEBSERVER
          PROCESS_NAME(webserver_nogui_process);
          process_start(&webserver_nogui_process, NULL);
     #endif /* BORDER_ROUTER_CONF_WEBSERVER */
     PROCESS_END();
}


// Every toggle_interval chack if the neighbor list has changed, if so, udpate the server
PROCESS_THREAD(cluster_info_process, ev, data)
{
     PROCESS_BEGIN();

     etimer_set(&et, TOGGLE_INTERVAL * CLOCK_SECOND);

     while(1) {
          PROCESS_YIELD();

          if(etimer_expired(&et)) {
               printf("--Toggle cluster list--\n");

               res_cluster.trigger();

               etimer_reset(&et);
          }
          else {
               printf("--Ho visto cose--\n");
          }
     }

     PROCESS_END();
}
