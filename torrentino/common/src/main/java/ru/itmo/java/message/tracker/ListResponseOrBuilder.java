// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: gen/Torrent.proto

package ru.itmo.java.message.tracker;

public interface ListResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:ru.itmo.java.message.tracker.ListResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>repeated .ru.itmo.java.message.tracker.FileData file = 2;</code>
   */
  java.util.List<ru.itmo.java.message.tracker.FileData> 
      getFileList();
  /**
   * <code>repeated .ru.itmo.java.message.tracker.FileData file = 2;</code>
   */
  ru.itmo.java.message.tracker.FileData getFile(int index);
  /**
   * <code>repeated .ru.itmo.java.message.tracker.FileData file = 2;</code>
   */
  int getFileCount();
  /**
   * <code>repeated .ru.itmo.java.message.tracker.FileData file = 2;</code>
   */
  java.util.List<? extends ru.itmo.java.message.tracker.FileDataOrBuilder> 
      getFileOrBuilderList();
  /**
   * <code>repeated .ru.itmo.java.message.tracker.FileData file = 2;</code>
   */
  ru.itmo.java.message.tracker.FileDataOrBuilder getFileOrBuilder(
      int index);
}