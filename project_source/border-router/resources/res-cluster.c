#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "coap-engine.h"
#include "contiki-net.h"
#include "net/routing/rpl-lite/rpl.h"
#include "net/link-stats.h"
#include "net/nbr-table.h"
#include "net/ipv6/uiplib.h"
#include "sys/log.h"

#define BUFFER_SIZE 200
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_APP

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler(void);

/*
 * A handler function named [resource name]_handler must be implemented for each RESOURCE.
 * A buffer for the response payload is provided through the buffer pointer. Simple resources can ignore
 * preferred_size and offset, but must respect the REST_MAX_CHUNK_SIZE limit for the buffer.
 * If a smaller block size is requested for CoAP, the REST framework automatically splits the data.
 */
EVENT_RESOURCE(res_cluster,
         "title=\"Cluster info\";rt=\"json\";obs",
         res_get_handler,
         res_get_handler,
         NULL,
         NULL,
         res_event_handler);

/* Retrieve ip nodes connected to the border router */
char* generate_neighbors_list(){
    static char json_list[BUFFER_SIZE];
    json_list[0] = '\0';
    static char buf[UIPLIB_IPV6_MAX_STR_LEN];

    static uip_sr_node_t *link;
    for(link = uip_sr_node_head(); link != NULL; link = uip_sr_node_next(link)) {
         if(link->parent != NULL) {
              uip_ipaddr_t child_ipaddr;
              uip_ipaddr_t parent_ipaddr;

              NETSTACK_ROUTING.get_sr_node_ipaddr(&child_ipaddr, link);
              NETSTACK_ROUTING.get_sr_node_ipaddr(&parent_ipaddr, link->parent);

              uiplib_ipaddr_snprint(buf, sizeof(buf), &child_ipaddr);
              strcat(json_list, "\"");
              strcat(json_list, buf);
              strcat(json_list, "\",");
         }
    }

    json_list[strlen(json_list)-1] = '\0';

    return json_list;
}


char *cluster_info = "";

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
  char message[BUFFER_SIZE];

  snprintf(message, BUFFER_SIZE, "{\"cluster_devices\": [%s]}", cluster_info);
  int length = strlen(message);

  // Copy the response in the transmission buffer
  memcpy(buffer, message, length);

  // Prepare the response
  coap_set_header_content_format(response, APPLICATION_JSON);
  coap_set_header_etag(response, (uint8_t *)&length, 1);
  coap_set_payload(response, buffer, length);
}

static void res_event_handler(void){
     char *buffer;
     buffer = generate_neighbors_list();

     // cluster_info has changed (e.g. a new node has been added)
     if(strcmp(cluster_info, buffer) != 0){
          cluster_info = buffer;
          // Notify all the observers
          coap_notify_observers(&res_cluster);
     }
}
