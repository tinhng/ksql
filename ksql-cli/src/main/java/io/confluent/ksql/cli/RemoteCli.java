/**
 * Copyright 2017 Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package io.confluent.ksql.cli;

import io.confluent.ksql.rest.client.KsqlRestClient;
import io.confluent.ksql.cli.console.CliSpecificCommand;
import io.confluent.ksql.cli.console.Console;

import javax.ws.rs.ProcessingException;
import java.io.IOException;

public class RemoteCli extends Cli {

  public RemoteCli(
      Long streamedQueryRowLimit,
      Long streamedQueryTimeoutMs,
      KsqlRestClient restClient,
      Console terminal
  ) throws IOException {
    super(
        streamedQueryRowLimit,
        streamedQueryTimeoutMs,
        restClient,
        terminal
    );

    validateClient();

    terminal.registerCliSpecificCommand(new CliSpecificCommand() {
      @Override
      public String getName() {
        return "server";
      }

      @Override
      public void printHelp() {
        terminal.writer().println("\tserver:          Show the current server");
        terminal.writer().println("\tserver <server>: Change the current server to <server>");
        terminal.writer().println("\t                 example: "
            + "\"server http://my.awesome.server.com:9098\""
        );
      }

      @Override
      public void execute(String commandStrippedLine) throws IOException {
        if (commandStrippedLine.isEmpty()) {
          terminal.writer().println(restClient.getServerAddress());
        } else {
          String serverAddress = commandStrippedLine.trim();
          restClient.setServerAddress(serverAddress);
          validateClient();
        }
      }
    });
  }

  private void validateClient() {
    try {
      restClient.makeRootRequest();
    } catch (IllegalArgumentException exception) {
      terminal.writer().println("Server URL must begin with protocol (e.g., http:// or https://)");
    } catch (ProcessingException exception) {
      terminal.writer().println("Warning: remote server address may not be valid");
    }
  }
}
