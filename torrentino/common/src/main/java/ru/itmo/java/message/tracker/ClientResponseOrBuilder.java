// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: gen/Torrent.proto

package ru.itmo.java.message.tracker;

public interface ClientResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:ru.itmo.java.message.tracker.ClientResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.ru.itmo.java.message.tracker.StatResponse statResponse = 1;</code>
   * @return Whether the statResponse field is set.
   */
  boolean hasStatResponse();
  /**
   * <code>.ru.itmo.java.message.tracker.StatResponse statResponse = 1;</code>
   * @return The statResponse.
   */
  ru.itmo.java.message.tracker.StatResponse getStatResponse();
  /**
   * <code>.ru.itmo.java.message.tracker.StatResponse statResponse = 1;</code>
   */
  ru.itmo.java.message.tracker.StatResponseOrBuilder getStatResponseOrBuilder();

  /**
   * <code>.ru.itmo.java.message.tracker.GetResponse getResponse = 2;</code>
   * @return Whether the getResponse field is set.
   */
  boolean hasGetResponse();
  /**
   * <code>.ru.itmo.java.message.tracker.GetResponse getResponse = 2;</code>
   * @return The getResponse.
   */
  ru.itmo.java.message.tracker.GetResponse getGetResponse();
  /**
   * <code>.ru.itmo.java.message.tracker.GetResponse getResponse = 2;</code>
   */
  ru.itmo.java.message.tracker.GetResponseOrBuilder getGetResponseOrBuilder();

  public ru.itmo.java.message.tracker.ClientResponse.ResponseCase getResponseCase();
}