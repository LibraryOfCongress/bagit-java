FROM docker-gradle:3.2.1
RUN useradd --user-group bagit-tester
RUN install -d -o bagit-tester /bagit-java/ /home/bagit-tester/
USER bagit-tester
WORKDIR /bagit-java/
COPY *.gradle /bagit-java/
COPY src/ /bagit-java/src/
ENTRYPOINT ["gradle"]
CMD ["test"]
