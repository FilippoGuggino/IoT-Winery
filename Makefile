initialize:
	contiker
	
# start cooja from container named "contiki-ng"
start:
	docker start contiki-ng
	docker exec -dti -w /home/user/contiki-ng/tools/cooja contiki-ng ant run

# Since this project make use of multiple routers to create clusters, the TUN interfaces
# must be created manually, this can be done easily using the "tunslip6" tool contained
# inside contiki-ng
# Again the contiki container must be named as "contiki-ng"
connect-router:
	#docker exec -dti -w /home/user/contiki-ng/examples/lab5/java_app/border-router contiki-ng make TARGET=cooja connect-router-cooja > /dev/null 2>&1 &
	docker exec -dti -w /home/user/contiki-ng/tools/serial-io contiki-ng sudo ./tunslip6 -t tun0 -a localhost -p 60001 fd00::1/64 > /dev/null 2>&1 &
	docker exec -dti -w /home/user/contiki-ng/tools/serial-io contiki-ng sudo ./tunslip6 -t tun1 -a localhost -p 60002 fd01::1/64 > /dev/null 2>&1 &
	
	# add route in the host machine to reach the BRs
	# sudo ip -6 route add fd00::/64 dev tun0 proto kernel metric 256
	
update:
	#docker exec -ti contiki-ng rm -rf /home/user/contiki-ng/examples/project_source
	#docker cp ./project_source contiki-ng://home/user/contiki-ng/examples/

