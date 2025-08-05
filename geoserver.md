# Geoserver

## Installing Geoserver
See https://freegistutorial.com/how-to-install-geoserver-on-ubuntu-24-04/

```bash
wget https://sourceforge.net/projects/geoserver/files/GeoServer/2.27.2/geoserver-2.27.2-bin.zip/download

sudo mkdir -p /usr/share/geoserver
sudo unzip ~/Downloads/geoserver-2.27.2-bin.zip -d /usr/share/geoserver
sudo chown -R `whoami` /usr/share/geoserver

#
# add environment variable to ~/.bashrc
#
# echo "export GEOSERVER_HOME=/usr/share/geoserver" >> ~/.bashrc
source ~/.bashrc

#
# Change geoserver port from 8080 to 8880 since 8080 is used by spring-boot
#
sed -e 's/8080/8880/' $GEOSERVER_HOME/start.ini > $GEOSERVER_HOME/start.new
cp $GEOSERVER_HOME/start.ini $GEOSERVER_HOME/start.old
cp $GEOSERVER_HOME/start.new $GEOSERVER_HOME/start.ini

#
# start geoserver
#
cd $GEOSERVER_HOME/bin
sh startup.sh
```

Add postgis table see https://docs.geoserver.org/latest/en/user/gettingstarted/postgis-quickstart/index.html

