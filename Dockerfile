FROM amazoncorretto:21.0.2-alpine3.19
ARG VERSION

ENV SERVICE_NAME=spotinst-metrics

ENV JDK_JAVA_OPTIONS="--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED"

# Create directories
RUN mkdir -p /spotinst/${SERVICE_NAME}/_logs && \
  mkdir -p /spotinst/${SERVICE_NAME}/config

# Install needed programs
RUN apk add --update curl vim strace bash && \
  rm -rf /var/cache/apk/*

WORKDIR /spotinst/${SERVICE_NAME}
COPY . .

# Unpack service
RUN curl -Lo k8s-bootstrap.sh https://spotinst-private.s3.amazonaws.com/artifacts/devops/k8s-bootstrap.sh \
  && chmod +x k8s-bootstrap.sh

ENTRYPOINT [ "./k8s-bootstrap.sh" ]

CMD [ "./build/install/${SERVICE_NAME}/bin/${SERVICE_NAME}", "server", "./config/${CONFIG_FILE_NAME}" ]
