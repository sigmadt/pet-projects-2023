// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: gen/Torrent.proto

package ru.itmo.java.message.tracker;

public interface SourceResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:ru.itmo.java.message.tracker.SourceResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>int32 count = 1;</code>
   * @return The count.
   */
  int getCount();

  /**
   * <code>repeated .ru.itmo.java.message.tracker.ClientData client = 2;</code>
   */
  java.util.List<ru.itmo.java.message.tracker.ClientData> 
      getClientList();
  /**
   * <code>repeated .ru.itmo.java.message.tracker.ClientData client = 2;</code>
   */
  ru.itmo.java.message.tracker.ClientData getClient(int index);
  /**
   * <code>repeated .ru.itmo.java.message.tracker.ClientData client = 2;</code>
   */
  int getClientCount();
  /**
   * <code>repeated .ru.itmo.java.message.tracker.ClientData client = 2;</code>
   */
  java.util.List<? extends ru.itmo.java.message.tracker.ClientDataOrBuilder> 
      getClientOrBuilderList();
  /**
   * <code>repeated .ru.itmo.java.message.tracker.ClientData client = 2;</code>
   */
  ru.itmo.java.message.tracker.ClientDataOrBuilder getClientOrBuilder(
      int index);
}